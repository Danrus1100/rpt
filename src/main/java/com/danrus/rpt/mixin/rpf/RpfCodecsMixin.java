package com.danrus.rpt.mixin.rpf;

import com.danrus.rpf.RpfCodecs;
import com.danrus.rpf.api.codec.RpfModelsCodecsExtends;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RpfCodecs.class)
public class RpfCodecsMixin {
    @Inject(
            method = "init",
            at = @At("HEAD")
    )
    private static void rpt$initCodecs(CallbackInfo ci) {
        RpfModelsCodecsExtends.getInstance().register(
                Identifier.withDefaultNamespace("model"),
                Identifier.CODEC.optionalFieldOf("rpt$patch"),
                (o, resourceLocation) -> {
                    if ()
                }
        );
    }
}
