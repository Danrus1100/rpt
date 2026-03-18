package com.danrus.rpt.core.textures;

import com.danrus.rpt.core.textures.swappers.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

public class TextureSwappers {
    public static final ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends TextureSwapper.Unbaked>> ID_MAPPER = new ExtraCodecs.LateBoundIdMapper<>();
    public static final Codec<TextureSwapper.Unbaked> CODEC = ID_MAPPER.codec(Identifier.CODEC).dispatch(TextureSwapper.Unbaked::type, mapCodec -> mapCodec);


    public static void bootstrap() {
        ID_MAPPER.put(
                Identifier.fromNamespaceAndPath("rpt", "apply"),
                SwapperApplier.MAP_CODEC
        );
        ID_MAPPER.put(
                Identifier.fromNamespaceAndPath("rpt", "component"),
                ByComponentSwapper.MAP_CODEC
        );
        ID_MAPPER.put(
                Identifier.fromNamespaceAndPath("rpt", "empty"),
                EmptySwapper.INSTANCE.type()
        );
        ID_MAPPER.put(
                Identifier.fromNamespaceAndPath("rpt", "expression"),
                ExpressionSwapper.Unbaked.MAP_CODEC
        );

        ID_MAPPER.put(
                Identifier.fromNamespaceAndPath("rpt", "composite"),
                CompositeSwapper.Unbaked.MAP_CODEC
        );
    }
}
