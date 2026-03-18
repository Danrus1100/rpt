package com.danrus.rpt.core.textures.swappers;

import com.danrus.rpt.core.textures.SwapApplier;
import com.danrus.rpt.core.textures.TextureSwapper;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record SwapperApplier(Identifier location) implements TextureSwapper, TextureSwapper.Unbaked {

    public static MapCodec<SwapperApplier> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Identifier.CODEC.fieldOf("texture").forGetter(SwapperApplier::location)
    ).apply(i, SwapperApplier::new));

    @Override
    public void swap(ItemStack stack, @Nullable LivingEntity entity, List<Identifier> pendingSwapperApply) {
        pendingSwapperApply.add(location);
    }

    @Override
    public MapCodec<? extends TextureSwapper.Unbaked> type() {
        return MAP_CODEC;
    }

    @Override
    public TextureSwapper bake() {
        return new SwapperApplier(location);
    }
}
