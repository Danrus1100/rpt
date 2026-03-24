package com.danrus.rpt.mixin.load;

import com.danrus.rpt.core.bbmodel.fsm.FsmTriggers;
import com.danrus.rpt.core.selection.NestedSelectorsBootstrap;
import net.minecraft.client.ClientBootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientBootstrap.class)
public class ClientBootstrapsMixin {
    @Inject(
            method = "bootstrap",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemModels;bootstrap()V")
    )
    private static void rpt$clientBoot(CallbackInfo ci) {
        NestedSelectorsBootstrap.bootstrap();
        FsmTriggers.bootstrap();
    }
}
