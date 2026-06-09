package com.danrus.rpt.core.meta;

import com.danrus.rpt.duck.RptMetaHolder;
import com.danrus.rpt.utils.RptCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.StrictJsonParser;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;

public record RptMeta(boolean enable) {
    public static final RptMeta EMPTY = new RptMeta(false);

    public static final String EXTENSION = ".json";
    public static final String FILENAME = "rptmeta.json";

    public static final Codec<RptMeta> CODEC = RptCodecs.unit(new RptMeta(true));

    @Nullable
    public static RptMeta load(final PackResources packResources) throws IOException {
        IoSupplier<InputStream> meta = packResources.getRootResource(FILENAME);
        if (meta == null) return EMPTY;
        InputStream resource = meta.get();
        RptMeta rptMeta;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8));) {
            rptMeta = CODEC.parse(JsonOps.INSTANCE, StrictJsonParser.parse(reader)).getOrThrow();
        } catch (Throwable e) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Throwable var5) {
                    e.addSuppressed(var5);
                }
            }

            throw e;
        }

        if (resource != null) {
            resource.close();
        }

        return rptMeta;
    }

    @Nullable
    public static RptMeta from(Object object) {
        if (object instanceof RptMetaHolder holder) {
            return holder.rpt$getMeta();
        } else {
            throw new IllegalArgumentException("RptMeta: wrong holder presents, got " + object.getClass().getSimpleName());
        }
    }

    public static void set(Object object, RptMeta meta) {
        if (object instanceof RptMetaHolder holder) {
            holder.rpt$setMeta(meta);
        } else {
            throw new IllegalArgumentException("RptMeta: wrong holder presents, got " + object.getClass().getSimpleName());
        }
    }
}
