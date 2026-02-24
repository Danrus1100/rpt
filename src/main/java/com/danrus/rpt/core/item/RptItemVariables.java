package com.danrus.rpt.core.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record RptItemVariables(Map<String, String> strings, Map<String, Double> numbers, Map<String, Boolean> flags, Map<String, ResourceLocation> models) {

    public static final Codec<RptItemVariables> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.STRING)
                    .optionalFieldOf("strings", Map.of())
                    .forGetter(RptItemVariables::strings),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE)
                    .optionalFieldOf("numbers", Map.of())
                    .forGetter(RptItemVariables::numbers),
            Codec.unboundedMap(Codec.STRING, Codec.BOOL)
                    .optionalFieldOf("flags", Map.of())
                    .forGetter(RptItemVariables::flags),
            Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC)
                    .optionalFieldOf("models", Map.of())
                    .forGetter(RptItemVariables::models)
    ).apply(instance, RptItemVariables::new));

    public static final RptItemVariables EMPTY = new RptItemVariables(Map.of(), Map.of(), Map.of(), Map.of());

    public <T> T get(Type<T> type, String name) {
        if (type == Type.STRING) {
            return type.clazz.cast(strings.get(name));
        } else if (type == Type.NUMBER) {
            return type.clazz.cast(numbers.get(name));
        } else if (type == Type.FLAG) {
            return type.clazz.cast(flags.get(name));
        } else if (type == Type.MODEL) {
            return type.clazz.cast(models.get(name));
        }
        throw new IllegalArgumentException("Unsupported variable type: " + type);
    }

    public RptItemVariables merge(RptItemVariables other) {
        Map<String, String> newStrings = new HashMap<>(this.strings);
        newStrings.putAll(other.strings);

        Map<String, Double> newNumbers = new HashMap<>(this.numbers);
        newNumbers.putAll(other.numbers);

        Map<String, Boolean> newFlags = new HashMap<>(this.flags);
        newFlags.putAll(other.flags);

        Map<String, ResourceLocation> newModels = new HashMap<>(this.models);
        newModels.putAll(other.models);

        return new RptItemVariables(
                Map.copyOf(newStrings),
                Map.copyOf(newNumbers),
                Map.copyOf(newFlags),
                Map.copyOf(newModels)
        );
    }

    public record Type<T>(Class<T> clazz, String name)  {
        public static final Codec<Type<?>> CODEC = Codec.STRING.comapFlatMap(
                Type::validate,
                Type::name
        );

        public static Type<?> getOrThrow(String name) {
            return switch (name) {
                case "string" -> STRING;
                case "number" -> NUMBER;
                case "flag" -> FLAG;
                case "model" -> MODEL;
                default -> throw new IllegalArgumentException("Unknown variable type:" + name);
            };
        }

        public Codec<?> codecOrThrow() {
            return switch (name) {
                case "string" -> Codec.STRING;
                case "number" -> Codec.DOUBLE;
                case "flag" -> Codec.BOOL;
                case "model" -> ResourceLocation.CODEC;
                default -> throw new IllegalArgumentException("Unknown variable type:" + this);
            };
        }

        @Override
        public @NotNull String toString() {
            return "Type{" +
                    "name='" + name + '\'' +
                    '}';
        }

        public static DataResult<Type<?>> validate(String name) {
            try {
                return DataResult.success(getOrThrow(name));
            } catch (Exception e) {
                return DataResult.error(e::getMessage);
            }
        }

        public static final Type<String> STRING = new Type<>(String.class, "string");
        public static final Type<Double> NUMBER = new Type<>(Double.class, "number");
        public static final Type<Boolean> FLAG = new Type<>(Boolean.class, "flag");
        public static final Type<ResourceLocation> MODEL = new Type<>(ResourceLocation.class, "model");
    }
}