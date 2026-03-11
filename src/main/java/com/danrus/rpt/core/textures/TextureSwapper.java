package com.danrus.rpt.core.textures;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface TextureSwapper {
    /**
     * Return texture form resource-generated wrappers.
     * @param stack related stack
     * @param entity related entity
     * @return swaped texture. Null if it don't need that
     */
    @Nullable ResourceLocation swap(ItemStack stack, @Nullable LivingEntity entity);

    MapCodec<? extends TextureSwapper> type();
}
