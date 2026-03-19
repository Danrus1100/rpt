package com.danrus.rpt.core.selection.type;

import com.danrus.rpt.core.selection.NestedSelector;
import com.danrus.rpt.core.selection.NestedSelectors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public record CompositeSelector<T>(List<NestedSelector<T>> swappers) implements NestedSelector<T> {
    @Override
    public void resolveSelect(ItemStack stack, @Nullable LivingEntity entity, Consumer<T> callback) {
        swappers.forEach(selector -> selector.resolveSelect(stack, entity, callback));
    }

    public static record Unbaked<T>(List<NestedSelector.Unbaked<T>> swappers) implements NestedSelector.Unbaked<T> {
        private static final Map<Codec<?>, MapCodec<?>> CODECS = new IdentityHashMap<>();

        @SuppressWarnings("unchecked")
        public static synchronized <T> MapCodec<Unbaked<T>> codec(Codec<T> valueCodec) {
            return RecordCodecBuilder.mapCodec(unbakedInstance -> unbakedInstance.group(
                    NestedSelectors.codec(valueCodec).listOf().fieldOf("children").forGetter(Unbaked::swappers)
            ).apply(unbakedInstance, Unbaked::new));
        }

        @Override
        public NestedSelector<T> bake() {
            return new CompositeSelector<>(swappers.stream().map(NestedSelector.Unbaked::bake).toList());
        }

        @Override
        public MapCodec<? extends NestedSelector.Unbaked<T>> type(Codec<T> valueCodec) {
            return codec(valueCodec);
        }
    }
}
