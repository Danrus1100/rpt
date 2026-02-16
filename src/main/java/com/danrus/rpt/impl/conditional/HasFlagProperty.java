package com.danrus.rpt.impl.conditional;

import com.danrus.rpt.duck.RptItemParamsHolder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record HasFlagProperty(String property) implements ConditionalItemModelProperty {

    public static final MapCodec<HasFlagProperty> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("flag").forGetter(HasFlagProperty::property)
    ).apply(instance, HasFlagProperty::new));

    @Override
    public MapCodec<? extends ConditionalItemModelProperty> type() {
        return MAP_CODEC;
    }

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
        return RptItemParamsHolder.class.cast(stack).rpt$getParams().map(params -> params.hasFlag(property)).orElse(false);
    }
}
