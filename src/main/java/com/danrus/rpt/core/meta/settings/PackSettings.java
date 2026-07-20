package com.danrus.rpt.core.meta.settings;

import com.danrus.rpt.core.meta.settings.options.OptionType;
import com.danrus.rpt.core.meta.settings.options.OptionTypes;
import com.danrus.rpt.core.meta.settings.options.OptionTypesBootstrap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class PackSettings {
    static {
        OptionTypesBootstrap.bootstrap();
    }

    private static final Codec<Map<ResourceLocation, OptionType<?>>> OPTIONS_CODEC =
            Codec.unboundedMap(ResourceLocation.CODEC, OptionTypes.codec());

    public static final Codec<PackSettings> CODEC = OPTIONS_CODEC.xmap(
            PackSettings::new,
            PackSettings::optionMap
    );
    public static final MapCodec<PackSettings> MAP_CODEC = CODEC.fieldOf("settings");

    private final Map<ResourceLocation, Entry<?>> entries;

    public PackSettings() {
        this(Map.of());
    }

    public PackSettings(Map<ResourceLocation, OptionType<?>> options) {
        Map<ResourceLocation, Entry<?>> entries = new LinkedHashMap<>();
        options.forEach((id, option) -> entries.put(id, createEntry(id, option)));
        this.entries = Collections.unmodifiableMap(entries);
    }

    public Collection<Entry<?>> entries() {
        return entries.values();
    }

    public Optional<Entry<?>> entry(ResourceLocation id) {
        return Optional.ofNullable(entries.get(id));
    }

    public Entry<?> requireEntry(ResourceLocation id) {
        Entry<?> entry = entries.get(id);
        if (entry == null) {
            throw new IllegalArgumentException("Unknown RPT setting: " + id);
        }
        return entry;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public Map<ResourceLocation, OptionType<?>> optionMap() {
        Map<ResourceLocation, OptionType<?>> options = new LinkedHashMap<>();
        entries.forEach((id, entry) -> options.put(id, entry.option()));
        return Collections.unmodifiableMap(options);
    }

    private static <T> Entry<T> createEntry(ResourceLocation id, OptionType<T> option) {
        return new Entry<>(id, option);
    }

    public static final class Entry<T> {
        private final ResourceLocation id;
        private final OptionType<T> option;

        private Entry(ResourceLocation id, OptionType<T> option) {
            this.id = Objects.requireNonNull(id, "id");
            this.option = Objects.requireNonNull(option, "option");
        }

        public ResourceLocation id() {
            return id;
        }

        public OptionType<T> option() {
            return option;
        }

        public T getValue() {
            return option.getValue();
        }

        public T getDefaultValue() {
            return option.getDefaultValue();
        }

        public void setValue(T value) {
            option.setValue(value);
        }

        public double getAsDouble() {
            return option.getAsDouble();
        }
    }
}
