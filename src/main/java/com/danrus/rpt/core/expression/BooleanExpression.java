package com.danrus.rpt.core.expression;

import com.danrus.rpt.core.bbmodel.fsm.FsmCondition;
import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.parser.ParseException;
import com.mojang.serialization.Codec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public record BooleanExpression(String raw) implements FsmCondition {

    public static final BooleanExpression TRUE = new BooleanExpression("true");
    public static final BooleanExpression FALSE = new BooleanExpression("false");

    public static final Codec<BooleanExpression> CODEC = Codec.STRING.xmap(
            BooleanExpression::new,
            e -> e.raw
    );

    public boolean getValue(Map<String, Double> additionalVars) {
        if (this == TRUE) return true;
        if (this == FALSE) return false;

        Expression expr = new Expression(raw);
        additionalVars.forEach(expr::with);

        try {
            return expr.evaluate().getBooleanValue();
        } catch (Exception e) {
            GameExpressionsHelper.logError(raw, e);
            return false;
        }
    }

    public boolean getValue() {
        return getValue(Map.of());
    }

    @Override
    public boolean getValueWithGame(Map<String, Double> vars,
                                    @Nullable ClientLevel level,
                                    @Nullable LivingEntity entity,
                                    int seed) {
        if (this == TRUE) return true;
        if (this == FALSE) return false;

        return GameExpressionsHelper.evaluateBoolean(raw, vars, level, entity, seed);
    }
}
