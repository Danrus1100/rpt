package com.danrus.rpt.core.textures.swappers;

import com.danrus.rpt.core.textures.TextureSwapper;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ByComponentSwapper<T>(
        DataComponentType<T> componentType,
        Object2ObjectMap<T, ResourceLocation> valueToTexture,
        @Nullable ResourceLocation fallback
) implements TextureSwapper {

    @Override
    public @Nullable ResourceLocation swap(ItemStack stack, @Nullable LivingEntity entity) {
        T value = stack.get(componentType);
        ResourceLocation result = valueToTexture.get(value);
        return result != null ? result : fallback;
    }

    @Override
    public MapCodec<? extends TextureSwapper> type() {
        return MAP_CODEC;
    }

    public record SwitchCase<T>(List<T> values, ResourceLocation texture) {
        public static <T> Codec<SwitchCase<T>> codec(Codec<T> valueCodec) {
            return RecordCodecBuilder.create(instance -> instance.group(
                    ExtraCodecs.nonEmptyList(ExtraCodecs.compactListCodec(valueCodec))
                            .fieldOf("when")
                            .forGetter(SwitchCase::values),
                    ResourceLocation.CODEC
                            .fieldOf("texture")
                            .forGetter(SwitchCase::texture)
            ).apply(instance, SwitchCase::new));
        }
    }

    @SuppressWarnings("unchecked")
    public static final MapCodec<ByComponentSwapper<?>> MAP_CODEC = createCodec();

    @SuppressWarnings("unchecked")
    private static MapCodec<ByComponentSwapper<?>> createCodec() {
        return BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec()
                .validate(type -> type.isTransient()
                        ? DataResult.error(() -> "Component " + type + " can't be serialized in texture swapper")
                        : DataResult.success(type))
                .dispatchMap(
                        "component",
                        swapper -> ((ByComponentSwapper<?>) swapper).componentType,
                        (DataComponentType<?> componentType) -> {
                            Codec<Object> valueCodec = (Codec<Object>) componentType.codecOrThrow();

                            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                                    SwitchCase.codec(valueCodec).listOf()
                                            .validate(ByComponentSwapper::validateCases)
                                            .fieldOf("cases")
                                            .forGetter(s -> {
                                                ByComponentSwapper<?> sw = (ByComponentSwapper<?>) s;
                                                List<SwitchCase<Object>> cases = new ArrayList<>();
                                                sw.valueToTexture.object2ObjectEntrySet()
                                                        .forEach(e -> cases.add(new SwitchCase<>(List.of(e.getKey()), e.getValue())));
                                                return cases;
                                            }),

                                    ResourceLocation.CODEC.optionalFieldOf("fallback")
                                            .forGetter(s -> Optional.ofNullable(((ByComponentSwapper<?>) s).fallback))
                            ).apply(instance, (cases, fallbackOpt) -> {
                                Object2ObjectMap<Object, ResourceLocation> map = new Object2ObjectOpenHashMap<>();
                                for (SwitchCase<Object> sc : cases) {
                                    for (Object v : sc.values()) {
                                        map.put(v, sc.texture());
                                    }
                                }
                                map.defaultReturnValue(fallbackOpt.orElse(null));

                                return new ByComponentSwapper<>(
                                        (DataComponentType<Object>) componentType,
                                        map,
                                        fallbackOpt.orElse(null)
                                );
                            }));
                        }
                );
    }

    private static <T> DataResult<List<SwitchCase<T>>> validateCases(List<SwitchCase<T>> cases) {
        if (cases.isEmpty()) {
            return DataResult.error(() -> "Texture swapper cases cannot be empty");
        }

        Multiset<T> seen = HashMultiset.create();
        for (SwitchCase<T> sc : cases) {
            seen.addAll(sc.values());
        }

        var duplicates = seen.entrySet().stream()
                .filter(e -> e.getCount() > 1)
                .map(e -> e.getElement().toString())
                .toList();

        if (!duplicates.isEmpty()) {
            return DataResult.error(() -> "Duplicate component values in cases: " + String.join(", ", duplicates));
        }
        return DataResult.success(cases);
    }
}