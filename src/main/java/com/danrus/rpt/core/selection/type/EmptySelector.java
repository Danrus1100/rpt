package com.danrus.rpt.core.selection.type;

import com.danrus.rpt.core.selection.NestedSelector;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class EmptySelector<T> implements NestedSelector<T>, NestedSelector.Unbaked<T> {

    private static final EmptySelector<?> INSTANCE = new EmptySelector<>();

    @SuppressWarnings("unchecked")
    public static <T> EmptySelector<T> instance() {
        return (EmptySelector<T>) INSTANCE;
    }

    public static <T> MapCodec<EmptySelector<T>> codec(Codec<T> valueCodec) {
        return MapCodec.unit(instance());
    }

    @Override
    public void resolveSelect(ItemStack stack, @Nullable LivingEntity entity, Consumer<T> callback) {}

    @Override
    public NestedSelector<T> bake() {
        return this;
    }

    @Override
    public MapCodec<? extends NestedSelector.Unbaked<T>> type(Codec<T> valueCodec) {
        return codec(valueCodec);
    }
}
