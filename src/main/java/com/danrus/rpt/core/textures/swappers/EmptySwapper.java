package com.danrus.rpt.core.textures.swappers;

import com.danrus.rpt.core.textures.TextureSwapper;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EmptySwapper implements TextureSwapper, TextureSwapper.Unbaked {

    public static final EmptySwapper INSTANCE = new EmptySwapper();

    @Override
    public void swap(ItemStack stack, @Nullable LivingEntity entity, List<Identifier> pendingSwapperApply) {}

    @Override
    public TextureSwapper bake() {
        return new EmptySwapper();
    }

    @Override
    public MapCodec<? extends Unbaked> type() {
        return MapCodec.unit(new EmptySwapper());
    }
}
