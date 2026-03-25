package com.danrus.rpt.mixin.rpf;

import com.danrus.rpf.RpfCodecs;
import com.danrus.rpf.api.codec.RpfModelsCodecsExtends;
import com.danrus.rpt.duck.PatchInformer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(RpfCodecs.class)
public class RpfCodecsMixin {
    @Inject(
            method = "init",
            at = @At("HEAD")
    )
    private static void rpt$initCodecs(CallbackInfo ci) {
        registerPatchInformer(Identifier.withDefaultNamespace("empty"));
        registerPatchInformer(Identifier.withDefaultNamespace("model"));
        registerPatchInformer(Identifier.withDefaultNamespace("range_dispatch"));
        registerPatchInformer(Identifier.withDefaultNamespace("special"));
        registerPatchInformer(Identifier.withDefaultNamespace("composite"));
        registerPatchInformer(Identifier.withDefaultNamespace("bundle/selected_item"));
        registerPatchInformer(Identifier.withDefaultNamespace("select"));
        registerPatchInformer(Identifier.withDefaultNamespace("condition"));
    }

    @Unique
    private static void registerPatchInformer(Identifier location) {
        RpfModelsCodecsExtends.getInstance().register(
                location,
                Identifier.CODEC.optionalFieldOf("rpt$patch"),
                (o, resourceLocation) -> {
                    if (o instanceof PatchInformer informer && resourceLocation.isPresent()) {
                        informer.rpt$setPatchPath(resourceLocation.orElse(null));
                    }
                },
                o -> {
                    if (o instanceof PatchInformer informer) {
                        return Optional.ofNullable(informer.rpt$getPatchPath());
                    }
                    return Optional.empty();
                }
        );
    }
}
