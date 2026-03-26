package com.danrus.rpt.core.bbmodel.fsm;

import com.danrus.rpt.core.expression.BooleanExpression;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@FunctionalInterface
public interface FsmCondition {

    static Codec<FsmCondition> CODEC = Codec.either(
            BooleanExpression.CODEC,
            Codec.BOOL
    ).xmap(
            either -> either.map(
                    expr -> (FsmCondition) expr,
                    ConstantCondition::new
            ),
            cond -> {
                if (cond instanceof BooleanExpression b) {
                    return Either.left(b);
                }
                if (cond instanceof ConstantCondition(boolean value)) {
                    return Either.right(value);
                }
                throw new IllegalStateException("Condition " + cond + " is not serializable! Use BooleanExpression or ConstantCondition");
            }
    );

    boolean getValueWithGame(Map<String, Double> additionalVars, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed);

    public record ConstantCondition(boolean value) implements FsmCondition {

        @Override
        public boolean getValueWithGame(Map<String, Double> vars,
                                        @Nullable ClientLevel level,
                                        @Nullable LivingEntity entity,
                                        int seed) {
            return value;
        }
    }
}
