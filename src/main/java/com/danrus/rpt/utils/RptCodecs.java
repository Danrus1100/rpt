package com.danrus.rpt.utils;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class RptCodecs {
    public static <A> Codec<A> unit(A defaultValue) {
        return MapCodec.unit(defaultValue).codec();
    }

    /**
     * Describes how a single field of a {@link MapCodec} should be re-serialized.
     * <p>
     * The object type stays the same; only the on-disk/network representation of
     * the field changes. {@code originalType} is how the base codec reads/writes
     * the field, {@code newType} is how you want to store it. {@code decoder}
     * converts your value back into the form the base codec expects (read side),
     * {@code encoder} converts the base value into your form (write side).
     *
     * @param <O> the original (internal) field type
     * @param <N> the new (external) field type
     */
    public record FieldRemap<O, N>(
            String key,
            Codec<O> originalType,
            Codec<N> newType,
            Function<N, O> decoder,
            Function<O, N> encoder) {

        /** External serialized value {@code ->} the form the base codec expects (read). */
        private <T> DataResult<T> toBase(DynamicOps<T> ops, T external) {
            return newType.decode(ops, external)
                    .map(Pair::getFirst)
                    .map(decoder)
                    .flatMap(o -> originalType.encodeStart(ops, o));
        }

        /** Value produced by the base codec {@code ->} your external form (write). */
        private <T> DataResult<T> fromBase(DynamicOps<T> ops, T internal) {
            return originalType.decode(ops, internal)
                    .map(Pair::getFirst)
                    .map(encoder)
                    .flatMap(n -> newType.encodeStart(ops, n));
        }
    }

    /**
     * Wraps an existing {@link MapCodec} and replaces the serialized
     * representation of one or more fields in a single pass, WITHOUT changing the
     * result type {@code A}.
     * <p>
     * The contents of a {@code MapCodec} are opaque, so the replacement works at
     * the serialized-data level: on read each remapped field is first decoded with
     * its {@code newType}, converted into the form the base codec expects, and only
     * then handed to the base codec; on write it is the other way around. This lets
     * you, for example, make a field that vanilla writes as an {@code int} be
     * read/written as a {@code String} while keeping the original object structure.
     *
     * @param base    the original codec
     * @param remaps  the fields to remap (keyed by {@link FieldRemap#key()})
     * @param <A> the object type (unchanged)
     */
    public static <A> MapCodec<A> remapFields(MapCodec<A> base, FieldRemap<?, ?>... remaps) {
        Map<String, FieldRemap<?, ?>> byKey = new HashMap<>();
        for (FieldRemap<?, ?> remap : remaps) {
            byKey.put(remap.key(), remap);
        }

        return new MapCodec<>() {
            @Override
            public <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
                DataResult<Map<String, T>> overrides = DataResult.success(new HashMap<>());
                for (FieldRemap<?, ?> remap : byKey.values()) {
                    final T raw = input.get(remap.key());
                    if (raw == null) {
                        // field is absent — let base handle it (optional/default)
                        continue;
                    }
                    overrides = overrides.flatMap(map -> remap.toBase(ops, raw).map(value -> {
                        map.put(remap.key(), value);
                        return map;
                    }));
                }
                return overrides.flatMap(map -> base.decode(ops,
                        map.isEmpty() ? input : replaceValues(ops, input, map)));
            }

            @Override
            public <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                DataResult<MapLike<T>> baseMap = base.codec()
                        .encodeStart(ops, input)
                        .flatMap(ops::getMap);

                DataResult<RecordBuilder<T>> result = baseMap.map(map -> {
                    RecordBuilder<T> builder = prefix;
                    for (Pair<T, T> entry : (Iterable<Pair<T, T>>) map.entries()::iterator) {
                        T k = entry.getFirst();
                        T v = entry.getSecond();
                        String ks = ops.getStringValue(k).result().orElse(null);
                        FieldRemap<?, ?> remap = ks == null ? null : byKey.get(ks);
                        if (remap != null) {
                            v = remap.fromBase(ops, v).result().orElse(v);
                        }
                        builder = builder.add(k, v);
                    }
                    return builder;
                });

                return result.result().orElse(prefix).withErrorsFrom(result);
            }

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return base.keys(ops);
            }
        };
    }

    /**
     * Convenience overload of {@link #remapFields} for a single field.
     *
     * @see FieldRemap
     */
    public static <A, O, N> MapCodec<A> remapField(
            MapCodec<A> base,
            String key,
            Codec<O> originalType,
            Codec<N> newType,
            Function<N, O> decoder,
            Function<O, N> encoder) {
        return remapFields(base, new FieldRemap<>(key, originalType, newType, decoder, encoder));
    }

    /**
     * Read-only MapLike view over {@code input} that overrides the values of the
     * given keys.
     */
    private static <T> MapLike<T> replaceValues(DynamicOps<T> ops, MapLike<T> input, Map<String, T> overrides) {
        return new MapLike<>() {
            @Override
            public T get(T k) {
                String ks = ops.getStringValue(k).result().orElse(null);
                return ks != null && overrides.containsKey(ks) ? overrides.get(ks) : input.get(k);
            }

            @Override
            public T get(String k) {
                return overrides.containsKey(k) ? overrides.get(k) : input.get(k);
            }

            @Override
            public Stream<Pair<T, T>> entries() {
                return input.entries().map(p -> {
                    String ks = ops.getStringValue(p.getFirst()).result().orElse(null);
                    return ks != null && overrides.containsKey(ks)
                            ? Pair.of(p.getFirst(), overrides.get(ks))
                            : p;
                });
            }
        };
    }
}
