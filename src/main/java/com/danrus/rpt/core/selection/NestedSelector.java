package com.danrus.rpt.core.selection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface NestedSelector<T> {

    void resolveSelect(ItemStack stack, @Nullable LivingEntity entity, Consumer<T> callback);

    interface Unbaked<T> {
        NestedSelector<T> bake();

        MapCodec<? extends Unbaked<T>> type(Codec<T> valueCodec);
    }
}