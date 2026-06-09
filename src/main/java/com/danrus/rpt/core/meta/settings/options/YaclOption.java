package com.danrus.rpt.core.meta.settings.options;

import com.danrus.rpf.api.codec.RpfCodecBuilder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class YaclOption<T> implements OptionType<T> {

    private T value;
    private final Component name;
    @Nullable
    private final Component description;
    private final List<Consumer<T>> hooks = new ArrayList<>();
    private final List<CodecField<?, ?>> codecFields;

    protected YaclOption(Component name, @Nullable Component description, List<CodecField<T, ?>> codecFields) {
        this.name = name;
        this.description = description;
        this.codecFields = codecFields;
    }

    @Override
    public MapCodec<? extends OptionType<T>> type() {
        //TODO: make codec
        MapCodec<YaclOption<T>> base = MapCodec.unit(null);
        RpfCodecBuilder<YaclOption<T>> codec = RpfCodecBuilder.of(base);
        for (var codecField : codecFields) {
            codec.withField(codecField.fieldCodec(), codecField.setter(), codecField.getter());
        }
        return codec.build();
    }

    @Override
    public void hook(Consumer<T> onChange) {
        hooks.add(onChange);
    }

    protected record CodecField<T, V>(MapCodec<V> fieldCodec, BiConsumer<T, V> setter, Function<T, V> getter){}
}
