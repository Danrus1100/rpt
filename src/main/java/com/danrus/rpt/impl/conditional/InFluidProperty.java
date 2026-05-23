package com.danrus.rpt.impl.conditional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public record InFluidProperty(TagKey<Fluid> fluidType) implements ConditionalItemModelProperty {

    public static final MapCodec<InFluidProperty> MAP_CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(
                    TagKey.codec(BuiltInRegistries.FLUID.key()).fieldOf("fluid").forGetter(InFluidProperty::fluidType)
            )
            .apply(i, InFluidProperty::new)
    );

    @Override
    public MapCodec<InFluidProperty> type() {
        return MAP_CODEC;
    }

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
        if (entity == null) return false;
        return !entity.isEyeInFluid(fluidType);
    }
}
