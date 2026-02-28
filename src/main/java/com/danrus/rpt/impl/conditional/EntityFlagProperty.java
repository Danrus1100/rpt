package com.danrus.rpt.impl.conditional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.Map;

public record EntityFlagProperty(String flag) implements ConditionalItemModelProperty {

    private static final Map<String, Predicate<LivingEntity>> FLAGS = Map.of(
            "on_fire", LivingEntity::isOnFire,
            "sneaking", LivingEntity::isShiftKeyDown,
            "sprinting", LivingEntity::isSprinting,
            "swimming", LivingEntity::isSwimming,
            "gliding", LivingEntity::isFallFlying,
            "climbing", LivingEntity::onClimbable,
            "is_player", (e) -> e instanceof Player
    );

    public static final MapCodec<EntityFlagProperty> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("flag").forGetter(EntityFlagProperty::flag)
    ).apply(instance, EntityFlagProperty::new));

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
        if (entity == null) return false;

        Predicate<LivingEntity> check = FLAGS.get(flag);
        return check != null && check.test(entity);
    }

    @Override
    public MapCodec<? extends ConditionalItemModelProperty> type() {
        return MAP_CODEC;
    }
}
