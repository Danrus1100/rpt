package com.danrus.rpt.core.selection;

import java.util.function.Consumer;

public record SelectionContext<T>(boolean allowFallbacks, Consumer<T> callback) {

    public static <T> SelectionContext<T> ignoringFallbacks(Consumer<T> callback) {
        return new SelectionContext<>(false, callback);
    }

    public static <T> SelectionContext<T> usingFallbacks(Consumer<T> callback) {
        return new SelectionContext<>(true, callback);
    }

    public void applyValue(T value) {
        callback.accept(value);
    }
}
