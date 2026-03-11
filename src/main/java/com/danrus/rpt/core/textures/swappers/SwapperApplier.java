package com.danrus.rpt.core.textures.swappers;

import com.danrus.rpt.core.textures.TextureSwapper;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record SwapperApplier(ResourceLocation location) implements TextureSwapper {

    public static MapCodec<SwapperApplier> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ResourceLocation.CODEC.fieldOf("texture").forGetter(SwapperApplier::location)
    ).apply(i, SwapperApplier::new));

    @Override
    public @Nullable ResourceLocation swap(ItemStack stack, @Nullable LivingEntity entity) {
        return location;
    }

    @Override
    public MapCodec<? extends TextureSwapper> type() {
        return MAP_CODEC;
    }
}
