package com.danrus.rpt.core.textures.swappers;

import com.danrus.rpt.core.textures.TextureSwapper;
import com.danrus.rpt.core.textures.TextureSwappers;
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
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ByComponentSwapper<T>(
        DataComponentType<T> componentType,
        Object2ObjectMap<T, TextureSwapper> valueToTexture,
        TextureSwapper fallback
) implements TextureSwapper{

    public static final MapCodec<ByComponentSwapper.Unbaked<?>> MAP_CODEC = Unbaked.MAP_CODEC;

    @Override
    public void swap(ItemStack stack, @Nullable LivingEntity entity, List<Identifier> applier) {
        T value = stack.get(componentType);
        TextureSwapper result = valueToTexture.get(value);
        TextureSwapper toSwap = result == null ? fallback : result;
        toSwap.swap(stack, entity, applier);
    }

    public static record Unbaked<T>(
            DataComponentType<T> componentType,
            Object2ObjectMap<T, TextureSwapper.Unbaked> valueToTexture,
            Optional<TextureSwapper.Unbaked> fallback
    ) implements TextureSwapper.Unbaked {
        @Override
        public TextureSwapper bake() {
            Object2ObjectMap<T, TextureSwapper> bakedMap = new Object2ObjectOpenHashMap<>();
            valueToTexture.object2ObjectEntrySet().forEach(e -> bakedMap.put(e.getKey(), e.getValue().bake()));

            TextureSwapper bakedFallback = fallback
                .map(TextureSwapper.Unbaked::bake)
                .orElse((stack, entity, pendingSwapperApply) -> {});

            return new ByComponentSwapper<>(componentType, bakedMap, bakedFallback);
        }

        @Override
        public MapCodec<? extends TextureSwapper.Unbaked> type() {
            return MAP_CODEC;
        }

        public record SwitchCase<T>(List<T> values, TextureSwapper.Unbaked swapper) {
            public static <T> Codec<SwitchCase<T>> codec(Codec<T> valueCodec) {
                return RecordCodecBuilder.create(instance -> instance.group(
                        ExtraCodecs.nonEmptyList(ExtraCodecs.compactListCodec(valueCodec))
                                .fieldOf("when")
                                .forGetter(SwitchCase::values),
                        TextureSwappers.CODEC
                                .fieldOf("swapper")
                                .forGetter(SwitchCase::swapper)
                ).apply(instance, SwitchCase::new));
            }
        }

        public static final MapCodec<ByComponentSwapper.Unbaked<?>> MAP_CODEC = createCodec();

        @SuppressWarnings("unchecked")
        private static MapCodec<ByComponentSwapper.Unbaked<?>> createCodec() {
            return BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec()
                    .validate(type -> type.isTransient()
                            ? DataResult.error(() -> "Component " + type + " can't be serialized in texture swapper")
                            : DataResult.success(type))
                    .dispatchMap(
                            "component",
                            swapper -> ((ByComponentSwapper.Unbaked<?>) swapper).componentType,
                            (DataComponentType<?> componentType) -> {
                                Codec<Object> valueCodec = (Codec<Object>) componentType.codecOrThrow();

                                return RecordCodecBuilder.mapCodec(instance -> instance.group(
                                        SwitchCase.codec(valueCodec).listOf()
                                                .validate(ByComponentSwapper.Unbaked::validateCases)
                                                .fieldOf("cases")
                                                .forGetter(s -> {
                                                ByComponentSwapper.Unbaked<?> sw = (ByComponentSwapper.Unbaked<?>) s;
                                                    List<SwitchCase<Object>> cases = new ArrayList<>();
                                                    sw.valueToTexture.object2ObjectEntrySet()
                                                            .forEach(e -> cases.add(new SwitchCase<>(List.of(e.getKey()), e.getValue())));
                                                    return cases;
                                                }),

                                        TextureSwappers.CODEC.optionalFieldOf("fallback")
                                            .forGetter(s -> ((ByComponentSwapper.Unbaked<?>) s).fallback)
                                ).apply(instance, (cases, fallbackOpt) -> {
                                        Object2ObjectMap<Object, TextureSwapper.Unbaked> map = new Object2ObjectOpenHashMap<>();
                                    for (SwitchCase<Object> sc : cases) {
                                        for (Object v : sc.values()) {
                                            map.put(v, sc.swapper());
                                        }
                                    }

                                        return new ByComponentSwapper.Unbaked<>(
                                            (DataComponentType<Object>) componentType,
                                            map,
                                            fallbackOpt
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
}