package com.danrus.rpt.core.selection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface NestedSelector<T> {

    void resolveSelect(ItemStack stack, @Nullable LivingEntity entity, SelectionContext<T> context);

    public record BakeResult<T>(NestedSelector<T> selector, boolean hasFallbacks){}

    interface Unbaked<T> {
        BakeResult<T> bakeResult();

        MapCodec<? extends Unbaked<T>> type(Codec<T> valueCodec);
    }

    interface SimpleUnbaked<T> extends Unbaked<T> {
        @Override
        default BakeResult<T> bakeResult() {
            return new BakeResult<>(bake(), false);
        }

        abstract NestedSelector<T> bake();
    }
}