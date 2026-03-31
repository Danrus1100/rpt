package com.danrus.rpt.mixin.meta;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Pack.class)
public class PackMixin {

    @WrapOperation(
            method = "readMetaAndCreate",
            at = @At(value = "NEW", target = "(Lnet/minecraft/server/packs/PackLocationInfo;Lnet/minecraft/server/packs/repository/Pack$ResourcesSupplier;Lnet/minecraft/server/packs/repository/Pack$Metadata;Lnet/minecraft/server/packs/PackSelectionConfig;)Lnet/minecraft/server/packs/repository/Pack;")
    )
    private static Pack rpt$parseRptMeta(PackLocationInfo location, Pack.ResourcesSupplier resources, Pack.Metadata metadata, PackSelectionConfig selectionConfig, Operation<Pack> original) {
        return original.call(location, resources, metadata, selectionConfig);
    }
}
