package com.danrus.rpt.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

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

    public record Type<T>(Class<T> clazz)  {
        public static final Type<String> STRING = new Type<>(String.class);
        public static final Type<Double> NUMBER = new Type<>(Double.class);
        public static final Type<Boolean> FLAG = new Type<>(Boolean.class);
        public static final Type<ResourceLocation> MODEL = new Type<>(ResourceLocation.class);
    }
}