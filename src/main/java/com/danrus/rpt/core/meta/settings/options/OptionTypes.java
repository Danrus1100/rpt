package com.danrus.rpt.core.meta.settings.options;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public final class OptionTypes {
    private static final ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends OptionType<?>>> ID_MAPPER =
            new ExtraCodecs.LateBoundIdMapper<>();
    private static final Codec<OptionType<?>> CODEC = ID_MAPPER
            .codec(ResourceLocation.CODEC)
            .dispatch(OptionType::type, mapCodec -> mapCodec);

    private OptionTypes() {}

    public static synchronized void register(ResourceLocation id, MapCodec<? extends OptionType<?>> mapCodec) {
        ID_MAPPER.put(id, mapCodec);
    }

    public static Codec<OptionType<?>> codec() {
        return CODEC;
    }

    public static ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends OptionType<?>>> mapper() {
        return ID_MAPPER;
    }
}
