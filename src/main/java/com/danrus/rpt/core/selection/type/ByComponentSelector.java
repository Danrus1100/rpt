package com.danrus.rpt.core.selection.type;

import com.danrus.rpt.core.selection.NestedSelector;
import com.danrus.rpt.core.selection.NestedSelectors;
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
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public record ByComponentSelector<C, T>(
        DataComponentType<C> componentType,
        Object2ObjectMap<C, NestedSelector<T>> valueToTexture,
        NestedSelector<T> fallback
) implements NestedSelector<T> {

    @SuppressWarnings("unchecked")
    public static <T> MapCodec<ByComponentSelector.Unbaked<?, T>> codec(Codec<T> valueCodec) {
        return (MapCodec<ByComponentSelector.Unbaked<?, T>>) Unbaked.codec(valueCodec);
    }

    @Override
    public void resolveSelect(ItemStack stack, @Nullable LivingEntity entity, Consumer<T> callback) {
        C value = stack.get(componentType);
        NestedSelector<T> result = valueToTexture.get(value);
        NestedSelector<T> toSwap = result == null ? fallback : result;
        toSwap.resolveSelect(stack, entity, callback);
    }

    public static record Unbaked<C, T>(
            DataComponentType<C> componentType,
            Object2ObjectMap<C, NestedSelector.Unbaked<T>> valueToSelector,
            Optional<NestedSelector.Unbaked<T>> fallback
    ) implements NestedSelector.Unbaked<T> {
        private static final Map<Codec<?>, MapCodec<?>> CODECS = new IdentityHashMap<>();

        @SuppressWarnings("unchecked")
        public static synchronized <T> MapCodec<Unbaked<?, T>> codec(Codec<T> valueCodec) {
            return createCodec(valueCodec);
        }

        @Override
        public NestedSelector<T> bake() {
            Object2ObjectMap<C, NestedSelector<T>> bakedMap = new Object2ObjectOpenHashMap<>();
            valueToSelector.object2ObjectEntrySet().forEach(e -> bakedMap.put(e.getKey(), e.getValue().bake()));

            NestedSelector<T> bakedFallback = fallback
                .map(NestedSelector.Unbaked::bake)
                .orElse((stack, entity, callback) -> {});

            return new ByComponentSelector<>(componentType, bakedMap, bakedFallback);
        }

        @Override
        public MapCodec<? extends NestedSelector.Unbaked<T>> type(Codec<T> valueCodec) {
            return codec(valueCodec);
        }

        public record SwitchCase<C, T>(List<C> values, NestedSelector.Unbaked<T> swapper) {
            public static <C, T> Codec<SwitchCase<C, T>> codec(Codec<C> caseValueCodec, Codec<T> resultValueCodec) {
                return RecordCodecBuilder.create(instance -> instance.group(
                        ExtraCodecs.nonEmptyList(ExtraCodecs.compactListCodec(caseValueCodec))
                                .fieldOf("when")
                                .forGetter(SwitchCase::values),
                        NestedSelectors.codec(resultValueCodec)
                                .fieldOf("child")
                                .forGetter(SwitchCase::swapper)
                ).apply(instance, SwitchCase::new));
            }
        }

        @SuppressWarnings("unchecked")
        private static <T> MapCodec<Unbaked<?, T>> createCodec(Codec<T> resultValueCodec) {
            return BuiltInRegistries.DATA_COMPONENT_TYPE.byNameCodec()
                    .validate(type -> type.isTransient()
                            ? DataResult.error(() -> "Component " + type + " can't be serialized in selector!")
                            : DataResult.success(type))
                    .dispatchMap(
                            "component",
                            swapper -> ((Unbaked<?, T>) swapper).componentType,
                            (DataComponentType<?> componentType) -> {
                                Codec<Object> componentValueCodec = (Codec<Object>) componentType.codecOrThrow();

                                return RecordCodecBuilder.mapCodec(instance -> instance.group(
                                        SwitchCase.codec(componentValueCodec, resultValueCodec).listOf()
                                                .validate(Unbaked::validateCases)
                                                .fieldOf("cases")
                                                .forGetter(s -> {
                                                    Unbaked<?, T> sw = (Unbaked<?, T>) s;
                                                    List<SwitchCase<Object, T>> cases = new ArrayList<>();
                                                    sw.valueToSelector.object2ObjectEntrySet()
                                                            .forEach(e -> cases.add(new SwitchCase<>(List.of(e.getKey()), e.getValue())));
                                                    return cases;
                                                }),

                                        NestedSelectors.codec(resultValueCodec).optionalFieldOf("fallback")
                                            .forGetter(s -> ((Unbaked<?, T>) s).fallback)
                                ).apply(instance, (cases, fallbackOpt) -> {
                                        Object2ObjectMap<Object, NestedSelector.Unbaked<T>> map = new Object2ObjectOpenHashMap<>();
                                    for (SwitchCase<Object, T> sc : cases) {
                                        for (Object v : sc.values()) {
                                            map.put(v, sc.swapper());
                                        }
                                    }

                                        return new Unbaked<>(
                                            (DataComponentType<Object>) componentType,
                                            map,
                                            fallbackOpt
                                    );
                                }));
                            }
                    );
        }

        private static <C, T> DataResult<List<SwitchCase<C, T>>> validateCases(List<SwitchCase<C, T>> cases) {
            if (cases.isEmpty()) {
                return DataResult.error(() -> "Selector cases cannot be empty");
            }

            Multiset<C> seen = HashMultiset.create();
            for (SwitchCase<C, T> sc : cases) {
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