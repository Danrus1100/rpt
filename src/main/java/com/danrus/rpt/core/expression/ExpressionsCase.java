package com.danrus.rpt.core.expression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Objects;

public abstract class ExpressionsCase<T> {

    private final List<NumberOrString> when;
    private final T value;
    private final boolean requireAll;

    protected ExpressionsCase(List<NumberOrString> when, T value, boolean requireAll) {
        this.when = List.copyOf(Objects.requireNonNull(when, "when"));
        this.value = Objects.requireNonNull(value, "value");
        this.requireAll = requireAll;
    }

    public List<NumberOrString> when() {
        return when;
    }

    public T value() {
        return value;
    }

    public boolean requireAll() {
        return requireAll;
    }


    public static <T> Codec<ExpressionsCase<T>> codec(Codec<T> valueCodec) {
        return codec(valueCodec, "value");
    }

    public static <T> Codec<ExpressionsCase<T>> codec(Codec<T> valueCodec, String valueFieldName) {
        return RecordCodecBuilder.create(instance -> instance.group(
                NumberOrString.LIST_CODEC.fieldOf("when").forGetter(ExpressionsCase::when),
                valueCodec.fieldOf(valueFieldName).forGetter(ExpressionsCase::value),
                Codec.BOOL.optionalFieldOf("all", false).forGetter(ExpressionsCase::requireAll)
        ).apply(instance, ExpressionsCase.Impl::new));
    }

    public final Codec<ExpressionsCase<T>> codec() {
        return codec(createValueCodec());
    }

    protected abstract Codec<T> createValueCodec();

    private static final class Impl<T> extends ExpressionsCase<T> {
        private Impl(List<NumberOrString> when, T value, boolean requireAll) {
            super(when, value, requireAll);
        }

        @Override
        protected Codec<T> createValueCodec() {
            throw new UnsupportedOperationException("Use ExpressionsCase.codec(valueCodec) for generic decoding");
        }
    }

}
