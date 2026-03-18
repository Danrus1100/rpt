package com.danrus.rpt.core.textures.swappers;

import com.danrus.rpt.core.textures.TextureSwapper;
import com.danrus.rpt.core.textures.TextureSwappers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record CompositeSwapper(List<TextureSwapper> swappers) implements TextureSwapper{
    @Override
    public void swap(ItemStack stack, @Nullable LivingEntity entity, List<Identifier> pendingSwapperApply) {
        swappers.forEach(swapper -> swapper.swap(stack, entity, pendingSwapperApply));
    }

    public static record Unbaked(List<TextureSwapper.Unbaked> swappers) implements TextureSwapper.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(unbakedInstance -> unbakedInstance.group(
                TextureSwappers.CODEC.listOf().fieldOf("swappers").forGetter(Unbaked::swappers)
        ).apply(unbakedInstance, Unbaked::new));

        @Override
        public TextureSwapper bake() {
            return new CompositeSwapper(swappers.stream().map(TextureSwapper.Unbaked::bake).toList());
        }

        @Override
        public MapCodec<? extends TextureSwapper.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
