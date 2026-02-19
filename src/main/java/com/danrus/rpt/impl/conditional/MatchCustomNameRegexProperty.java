package com.danrus.rpt.impl.conditional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record MatchCustomNameRegexProperty(String regex) implements ConditionalItemModelProperty {

    public static final MapCodec<MatchCustomNameRegexProperty> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("regex").forGetter(MatchCustomNameRegexProperty::regex)
    ).apply(instance, MatchCustomNameRegexProperty::new));

    @Override
    public @NotNull MapCodec<? extends ConditionalItemModelProperty> type() {
        return MAP_CODEC;
    }

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
        Component customNameComponent = stack.get(DataComponents.CUSTOM_NAME);
        if (customNameComponent == null) {
            return false;
        }
        String customName = customNameComponent.getString();
        return customName.matches(regex);
    }
}