package com.danrus.rpt.core.textures;

import com.danrus.rpt.core.textures.swappers.ByComponentSwapper;
import com.danrus.rpt.core.textures.swappers.SwapperApplier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public class TextureSwappers {
    public static final ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends TextureSwapper>> ID_MAPPER = new ExtraCodecs.LateBoundIdMapper<>();
    public static final Codec<TextureSwapper> CODEC = ID_MAPPER.codec(ResourceLocation.CODEC).dispatch(TextureSwapper::type, mapCodec -> mapCodec);


    public static void bootstrap() {
        ID_MAPPER.put(
                ResourceLocation.fromNamespaceAndPath("rpt", "apply"),
                SwapperApplier.MAP_CODEC
        );
        ID_MAPPER.put(
                ResourceLocation.fromNamespaceAndPath("rpt", "component"),
                ByComponentSwapper.MAP_CODEC
        );
    }
}
