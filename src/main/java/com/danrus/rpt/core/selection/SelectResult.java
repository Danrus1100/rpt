package com.danrus.rpt.core.selection;

public record SelectResult<T>(T result, boolean isFallback) {
    public static <T> SelectResult<T> ok(T result) {
        return new SelectResult<>(result, false);
    }

    public static <T> SelectResult<T> fallback(T result) {
        return new SelectResult<>(result, true);
    }
}
