package com.danrus.rpt.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record RptItemParams(Optional<List<String>> customFlags, RptItemVariables variables) {
    public static final Codec<RptItemParams> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(Codec.STRING)
                    .optionalFieldOf("custom_flags")
                    .forGetter(RptItemParams::customFlags),
            RptItemVariables.CODEC
                    .optionalFieldOf("variables", RptItemVariables.EMPTY)
                    .forGetter(RptItemParams::variables)
    ).apply(instance, RptItemParams::new));

    public static final RptItemParams EMPTY = new RptItemParams(Optional.empty(), RptItemVariables.EMPTY);

    public boolean hasFlag(String flag) {
        return customFlags.isPresent() && customFlags.get().contains(flag);
    }
}
