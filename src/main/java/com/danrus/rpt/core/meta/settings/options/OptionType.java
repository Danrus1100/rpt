package com.danrus.rpt.core.meta.settings.options;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import java.util.function.BiConsumer;

public interface OptionType<T> {
    MapCodec<? extends OptionType<?>> type();

    Codec<T> valueCodec();

    T getValue();

    T getDefaultValue();

    void setValue(T value);

    void hook(BiConsumer<T, T> onChange);

    default double asDouble(T value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof Boolean bool) {
            return bool ? 1.0D : 0.0D;
        }
        return 0.0D;
    }

    default double getAsDouble() {
        return asDouble(getValue());
    }
}
