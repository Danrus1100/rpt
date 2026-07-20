package com.danrus.rpt.core.meta;

import com.danrus.rpt.core.meta.settings.PackSettings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class RptDynamicVariables {
    private static final Logger LOGGER = LoggerFactory.getLogger(RptDynamicVariables.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final String DIRECTORY_NAME = "rpt";

    private final PackSettings settings;
    private final Map<ResourceLocation, Object> values = new LinkedHashMap<>();
    private final Map<ResourceLocation, DynamicDefinition> dynamicDefinitions = new LinkedHashMap<>();
    @Nullable
    private final Path file;
    private JsonObject persistedValues = new JsonObject();
    private boolean loading;

    public RptDynamicVariables() {
        this(new PackSettings(), null);
    }

    private RptDynamicVariables(PackSettings settings, @Nullable Path file) {
        this.settings = Objects.requireNonNull(settings, "settings");
        this.file = file;

        for (PackSettings.Entry<?> entry : settings.entries()) {
            attachEntry(entry);
        }
    }

    public static RptDynamicVariables load(String packId, PackSettings settings) {
        Path directory = FabricLoader.getInstance().getGameDir().resolve(DIRECTORY_NAME);
        return load(packId, settings, directory);
    }

    public static RptDynamicVariables load(String packId, PackSettings settings, Path directory) {
        Objects.requireNonNull(packId, "packId");
        Objects.requireNonNull(directory, "directory");

        Path file = directory.resolve(fileName(packId));
        RptDynamicVariables variables = new RptDynamicVariables(settings, file);
        try {
            variables.reload();
        } catch (IOException exception) {
            LOGGER.error("Unable to load RPT settings for pack '{}' from {}", packId, file, exception);
        }
        return variables;
    }

    public synchronized double get(ResourceLocation key) {
        return getDouble(key, 0.0D);
    }

    public synchronized Optional<Object> getValue(ResourceLocation key) {
        return Optional.ofNullable(values.get(key));
    }

    public synchronized <T> Optional<T> getValue(ResourceLocation key, Class<T> valueType) {
        Object value = values.get(key);
        return valueType.isInstance(value) ? Optional.of(valueType.cast(value)) : Optional.empty();
    }

    public synchronized double getDouble(ResourceLocation key, double fallback) {
        Object value = values.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof Boolean bool) {
            return bool ? 1.0D : 0.0D;
        }
        return fallback;
    }

    public synchronized String getString(ResourceLocation key, String fallback) {
        Object value = values.get(key);
        return value instanceof String string ? string : fallback;
    }

    public synchronized boolean getBoolean(ResourceLocation key, boolean fallback) {
        Object value = values.get(key);
        return value instanceof Boolean bool ? bool : fallback;
    }

    public synchronized Map<ResourceLocation, Object> values() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    public synchronized <T> T get(PackSettings.Entry<T> entry) {
        requireOwnedEntry(entry);
        return entry.getValue();
    }

    public synchronized <T> void set(PackSettings.Entry<T> entry, T value) {
        requireOwnedEntry(entry);
        entry.setValue(value);
    }

    public synchronized <T> T register(ResourceLocation key, Codec<T> codec, T defaultValue) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(codec, "codec");
        Objects.requireNonNull(defaultValue, "defaultValue");
        requireUnregisteredSetting(key);

        dynamicDefinitions.put(key, new DynamicDefinition(codec, defaultValue));
        T value = defaultValue;
        JsonElement persistedValue = persistedValues.get(key.toString());
        if (persistedValue != null && !(persistedValue instanceof JsonNull)) {
            value = decodeDynamicValue(key, codec, persistedValue).orElse(defaultValue);
        }
        values.put(key, value);
        return value;
    }

    public synchronized <T> void set(ResourceLocation key, T value, Codec<T> codec) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(codec, "codec");
        requireUnregisteredSetting(key);

        DynamicDefinition previous = dynamicDefinitions.get(key);
        Object defaultValue = previous == null ? value : previous.defaultValue();
        dynamicDefinitions.put(key, new DynamicDefinition(codec, defaultValue));
        values.put(key, value);
        if (!loading) {
            saveQuietly();
        }
    }

    public synchronized void set(ResourceLocation key, Object value) {
        requireUnregisteredSetting(key);
        if (dynamicDefinitions.containsKey(key)) {
            throw new IllegalArgumentException(
                    "Dynamic value '" + key + "' has a registered codec; use set(ResourceLocation, T, Codec<T>)"
            );
        }

        values.put(key, requirePersistableValue(value));
        if (!loading) {
            saveQuietly();
        }
    }

    public synchronized void reload() throws IOException {
        loading = true;
        try {
            persistedValues = new JsonObject();
            values.clear();
            for (PackSettings.Entry<?> entry : settings.entries()) {
                resetEntry(entry);
            }
            for (Map.Entry<ResourceLocation, DynamicDefinition> entry : dynamicDefinitions.entrySet()) {
                values.put(entry.getKey(), entry.getValue().defaultValue());
            }

            if (file == null || !Files.exists(file)) {
                return;
            }

            JsonElement root;
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                root = JsonParser.parseReader(reader);
            }

            if (root == null || root instanceof JsonNull) {
                return;
            }
            if (!root.isJsonObject()) {
                throw new IOException("RPT settings file must contain a JSON object: " + file);
            }

            persistedValues = root.getAsJsonObject().deepCopy();
            for (PackSettings.Entry<?> entry : settings.entries()) {
                JsonElement encodedValue = persistedValues.get(entry.id().toString());
                if (encodedValue != null && !(encodedValue instanceof JsonNull)) {
                    decodeEntry(entry, encodedValue);
                }
            }

            for (Map.Entry<String, JsonElement> persistedEntry : persistedValues.entrySet()) {
                ResourceLocation id = ResourceLocation.tryParse(persistedEntry.getKey());
                if (id == null || settings.entry(id).isPresent()) {
                    continue;
                }

                DynamicDefinition definition = dynamicDefinitions.get(id);
                Object dynamicValue = definition == null
                        ? readDynamicValue(persistedEntry.getValue())
                        : decodeUntypedDynamicValue(id, definition.codec(), persistedEntry.getValue())
                                .orElse(definition.defaultValue());
                if (dynamicValue != null) {
                    values.put(id, dynamicValue);
                }
            }
        } finally {
            loading = false;
        }
    }

    public synchronized void save() throws IOException {
        if (file == null) {
            return;
        }

        JsonObject output = persistedValues.deepCopy();
        for (PackSettings.Entry<?> entry : settings.entries()) {
            output.add(entry.id().toString(), encodeEntry(entry));
        }
        for (Map.Entry<ResourceLocation, Object> entry : values.entrySet()) {
            if (settings.entry(entry.getKey()).isEmpty()) {
                DynamicDefinition definition = dynamicDefinitions.get(entry.getKey());
                JsonElement encodedValue = definition == null
                        ? encodeDynamicValue(entry.getValue())
                        : encodeDynamicValue(entry.getKey(), definition.codec(), entry.getValue());
                output.add(entry.getKey().toString(), encodedValue);
            }
        }

        Files.createDirectories(file.getParent());
        Path temporaryFile = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temporaryFile, GSON.toJson(output), StandardCharsets.UTF_8);
        try {
            Files.move(
                    temporaryFile,
                    file,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING);
        }
        persistedValues = output;
    }

    public PackSettings settings() {
        return settings;
    }

    public Optional<Path> file() {
        return Optional.ofNullable(file);
    }

    private <T> void attachEntry(PackSettings.Entry<T> entry) {
        values.put(entry.id(), entry.getValue());
        entry.option().hook((oldValue, newValue) -> {
            synchronized (this) {
                values.put(entry.id(), newValue);
                if (!loading) {
                    saveQuietly();
                }
            }
        });
    }

    private <T> void resetEntry(PackSettings.Entry<T> entry) {
        entry.setValue(entry.getDefaultValue());
        values.put(entry.id(), entry.getValue());
    }

    private <T> void requireOwnedEntry(PackSettings.Entry<T> entry) {
        PackSettings.Entry<?> registered = settings.entry(entry.id()).orElse(null);
        if (registered != entry) {
            throw new IllegalArgumentException("The setting entry does not belong to this variable set: " + entry.id());
        }
    }

    private void requireUnregisteredSetting(ResourceLocation key) {
        if (settings.entry(key).isPresent()) {
            throw new IllegalArgumentException(
                    "Setting '" + key + "' has a registered option type; use set(PackSettings.Entry<T>, T)"
            );
        }
    }

    private static <T> void decodeEntry(PackSettings.Entry<T> entry, JsonElement encodedValue) {
        entry.option()
                .valueCodec()
                .parse(JsonOps.INSTANCE, encodedValue)
                .resultOrPartial(error -> LOGGER.warn("Invalid persisted value for '{}': {}", entry.id(), error))
                .ifPresent(entry::setValue);
    }

    private static <T> JsonElement encodeEntry(PackSettings.Entry<T> entry) throws IOException {
        try {
            return entry.option()
                    .valueCodec()
                    .encodeStart(JsonOps.INSTANCE, entry.getValue())
                    .getOrThrow();
        } catch (RuntimeException exception) {
            throw new IOException("Unable to encode RPT setting '" + entry.id() + "'", exception);
        }
    }

    private static <T> Optional<T> decodeDynamicValue(ResourceLocation key, Codec<T> codec, JsonElement value) {
        return codec
                .parse(JsonOps.INSTANCE, value)
                .resultOrPartial(error -> LOGGER.warn("Invalid persisted value for '{}': {}", key, error));
    }

    @SuppressWarnings("unchecked")
    private static Optional<Object> decodeUntypedDynamicValue(ResourceLocation key, Codec<?> codec, JsonElement value) {
        return decodeDynamicValue(key, (Codec<Object>) codec, value);
    }

    @SuppressWarnings("unchecked")
    private static JsonElement encodeDynamicValue(ResourceLocation key, Codec<?> codec, Object value) throws IOException {
        try {
            return ((Codec<Object>) codec).encodeStart(JsonOps.INSTANCE, value).getOrThrow();
        } catch (RuntimeException exception) {
            throw new IOException("Unable to encode dynamic RPT value '" + key + "'", exception);
        }
    }

    @Nullable
    private static Object readDynamicValue(JsonElement element) {
        if (!(element instanceof JsonPrimitive primitive)) {
            return element.deepCopy();
        }
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        }
        if (primitive.isNumber()) {
            return primitive.getAsDouble();
        }
        if (primitive.isString()) {
            return primitive.getAsString();
        }
        return primitive.deepCopy();
    }

    private static Object requirePersistableValue(Object value) {
        Objects.requireNonNull(value, "value");
        if (value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof JsonElement) {
            return value;
        }
        throw new IllegalArgumentException(
                "Unsupported dynamic value type: " + value.getClass().getName()
                        + ". Expected String, Number, Boolean or JsonElement"
        );
    }

    private static JsonElement encodeDynamicValue(Object value) {
        if (value instanceof JsonElement element) {
            return element.deepCopy();
        }
        return GSON.toJsonTree(value);
    }

    private void saveQuietly() {
        try {
            save();
        } catch (IOException exception) {
            LOGGER.error("Unable to save RPT settings to {}", file, exception);
        }
    }

    private static String fileName(String packId) {
        String sanitized = packId.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (sanitized.isBlank()) {
            sanitized = "pack";
        }
        if (!sanitized.equals(packId)) {
            sanitized += "-" + Integer.toUnsignedString(packId.hashCode(), 16);
        }
        return sanitized + ".json";
    }

    private record DynamicDefinition(Codec<?> codec, Object defaultValue) {}
}
