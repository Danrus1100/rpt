package com.danrus.rpt.core;

import com.ezylang.evalex.Expression;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

public record NumberOrString(String expression) {

    public static final NumberOrString ZERO = new NumberOrString(0f);

    public NumberOrString(float val) {
        this(String.valueOf(val));
    }

    public Expression toExpression() {
        return new Expression(this.expression());
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
}