package com.danrus.rpt.mixin.meta;

import com.danrus.rpt.core.meta.RptMeta;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;

@Mixin(Pack.class)
public class PackMixin {

    @Unique
    private static final Logger log = LoggerFactory.getLogger("RptMeta");

    @Inject(
            method = "readPackMetadata",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/PackResources;getMetadataSection(Lnet/minecraft/server/packs/metadata/MetadataSectionType;)Ljava/lang/Object;")
    )
    private static void rpt$parseRptMeta(PackLocationInfo location, Pack.ResourcesSupplier resources, int version, CallbackInfoReturnable<Pack.Metadata> cir, @Local PackResources packResources) {
        try {
            RptMeta.set(location, RptMeta.load(packResources));
        } catch (IOException e) {
            log.error("Unable to read RPT meta: {}", e.getMessage(), e);
        }
    }
}
