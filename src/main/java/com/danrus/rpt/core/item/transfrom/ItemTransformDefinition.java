package com.danrus.rpt.core.item.transfrom;

import com.danrus.rpt.core.expression.Vector3Expression;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record ItemTransformDefinition(Vector3Expression rotation, Vector3Expression translation, Vector3Expression scale) {

    public static final ItemTransformDefinition NONE = new ItemTransformDefinition(Vector3Expression.ZERO, Vector3Expression.ZERO, Vector3Expression.ONE);

    public static final Codec<ItemTransformDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
            Vector3Expression.CODEC.optionalFieldOf("rotation", Vector3Expression.ZERO).forGetter(ItemTransformDefinition::rotation),
            Vector3Expression.CODEC.optionalFieldOf("translation", Vector3Expression.ZERO).forGetter(ItemTransformDefinition::translation),
            Vector3Expression.CODEC.optionalFieldOf("scale", Vector3Expression.ONE).forGetter(ItemTransformDefinition::scale)
    ).apply(i, ItemTransformDefinition::new));

    public boolean isDefault() {
        return this.equals(NONE);
    }

    public ItemTransform evaluateWithGame(Map<String, Double> additionalVars, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        return new ItemTransform(
                rotation.evaluateWithGame(additionalVars, level, entity, seed),
                translation.evaluateWithGame(additionalVars, level, entity, seed),
                scale.evaluateWithGame(additionalVars, level, entity, seed)
        );
    }
}

