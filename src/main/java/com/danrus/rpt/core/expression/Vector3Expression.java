package com.danrus.rpt.core.expression;

import com.mojang.serialization.Codec;
import net.minecraft.Util;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;
import java.util.Map;

public record Vector3Expression(NumericExpression x, NumericExpression y, NumericExpression z) {

    public static Vector3Expression ZERO = new Vector3Expression(NumericExpression.ZERO, NumericExpression.ZERO, NumericExpression.ZERO);
    public static Vector3Expression ONE = new Vector3Expression(new NumericExpression(1f), new NumericExpression(1f), new NumericExpression(1f));

    public static final Codec<Vector3Expression> CODEC = NumericExpression.CODEC.listOf()
            .comapFlatMap(
                    input -> Util.fixedSize(input, 3).map(
                            nos -> new Vector3Expression(
                                    nos.get(0),
                                    nos.get(1),
                                    nos.get(2)
                            )
                    ),
                    vec -> List.of(
                            vec.x(),
                            vec.y(),
                            vec.z()
                    )
            );

    public Vector3fc evaluateWithGame(Map<String, Double> additionalVars, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        return new Vector3f(
                (float) GameExpressionsHelper.evaluate(x.expression(), additionalVars, level, entity, seed),
                (float) GameExpressionsHelper.evaluate(y.expression(), additionalVars, level, entity, seed),
                (float) GameExpressionsHelper.evaluate(z.expression(), additionalVars, level, entity, seed)
        );
    }

    @Override
    public @NotNull String toString() {
        return "Vector3Expression[" + x.toString() + ", " + y.toString() + ", " + z.toString() + "]";
    }
}
