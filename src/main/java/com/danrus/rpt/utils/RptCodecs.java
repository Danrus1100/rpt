package com.danrus.rpt.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.yacl3.api.Option;

public class RptCodecs {
    public static <A> Codec<A> unit(A defaultValue) {
        return MapCodec.unit(defaultValue).codec();
    }

    public static <A> Codec<Option<A>> option(A value) {
        RecordCodecBuilder.mapCodec(i -> i.group(

        ))
    }
}
