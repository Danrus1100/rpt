package com.danrus.rpt.core.expression;

import com.ezylang.evalex.Expression;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import java.util.List;
import java.util.Objects;

public record NumberOrString(String expression) {

    public static final NumberOrString ZERO = new NumberOrString(0f);

    public NumberOrString(float val) {
        this(String.valueOf(val));
    }

    public NumberOrString(double val) {
        this(String.valueOf(val));
    }

    public static final Codec<NumberOrString> CODEC = Codec.either(Codec.FLOAT, Codec.STRING).xmap(
            either -> either.map(
                    f -> new NumberOrString(String.valueOf(f)),
                    NumberOrString::new
            ),
            nos -> {
                try {
                    return Either.left(nos.expression().isEmpty() ? 0f : Float.parseFloat(nos.expression()));
                } catch (NumberFormatException e) {
                    return Either.right(nos.expression());
                }
            }
    );

    public static final Codec<List<NumberOrString>> LIST_CODEC =
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
        if (!(o instanceof NumberOrString that)) return false;
        return Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(expression);
    }
}