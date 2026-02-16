package com.danrus.rpt.mixin.regs;

import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectItemModelProperties.class)
public class SelectItemModelPropertiesMixin {
    @Inject(
            method = "bootstrap",
            at = @At("HEAD")
    )
    private static void rpt$bootstrap(CallbackInfo ci) {

    }
}
