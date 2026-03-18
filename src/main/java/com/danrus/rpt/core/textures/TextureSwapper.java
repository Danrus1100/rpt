package com.danrus.rpt.core.textures;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface TextureSwapper {
    /**
     * Return texture form resource-generated wrappers.
     * @param stack related stack
     * @param entity related entity
     * @param pendingSwapperApply texture location applier list. if you want to apply location, you should to add applier to list
     */
    void swap(ItemStack stack, @Nullable LivingEntity entity, List<Identifier> pendingSwapperApply);

    public interface Unbaked {
        TextureSwapper bake();

        MapCodec<? extends TextureSwapper.Unbaked> type();
    }
}
