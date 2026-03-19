package com.danrus.rpt.core.expression;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import java.util.List;
import java.util.Objects;

public record NumericExpression(String expression) {

    public static final NumericExpression ZERO = new NumericExpression(0f);

    public NumericExpression(float val) {
        this(String.valueOf(val));
    }

    public NumericExpression(double val) {
        this(String.valueOf(val));
    }

    public static final Codec<NumericExpression> CODEC = Codec.either(Codec.FLOAT, Codec.STRING).xmap(
            either -> either.map(
                    f -> new NumericExpression(String.valueOf(f)),
                    NumericExpression::new
            ),
            nos -> {
                try {
                    return Either.left(nos.expression().isEmpty() ? 0f : Float.parseFloat(nos.expression()));
                } catch (NumberFormatException e) {
                    return Either.right(nos.expression());
                }
            }
    );

    public static final Codec<List<NumericExpression>> LIST_CODEC =
            Codec.either(CODEC, CODEC.listOf()).xmap(
                    either -> either.map(
                            List::of,
                            list -> list
                    ),
                    lnos -> {
                        if (lnos == null || lnos.isEmpty()) {
                            return Either.left(ZERO);
                        }
                        if (lnos.size() == 1) {
                            return Either.left(lnos.getFirst());
                        }
                        return Either.right(lnos);
                    }
            );

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NumericExpression that)) return false;
        return Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(expression);
    }
}