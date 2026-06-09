package com.danrus.rpt.core.meta.settings.options;

import com.mojang.serialization.MapCodec;

import java.util.function.Consumer;

public interface OptionType<T> {
    MapCodec<? extends OptionType<T>> type();
    void hook(Consumer<T> onChange);
}
