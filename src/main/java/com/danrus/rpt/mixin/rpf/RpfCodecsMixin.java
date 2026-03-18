package com.danrus.rpt.mixin.rpf;

import com.danrus.rpf.RpfCodecs;
import com.danrus.rpf.api.codec.RpfModelsCodecsExtends;
import com.danrus.rpt.duck.PatchInformer;
import net.minecraft.resources.ResourceLocation;
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
        registerPatchInformer(ResourceLocation.withDefaultNamespace("empty"));
        registerPatchInformer(ResourceLocation.withDefaultNamespace("model"));
        registerPatchInformer(ResourceLocation.withDefaultNamespace("range_dispatch"));
        registerPatchInformer(ResourceLocation.withDefaultNamespace("special"));
        registerPatchInformer(ResourceLocation.withDefaultNamespace("composite"));
        registerPatchInformer(ResourceLocation.withDefaultNamespace("bundle/selected_item"));
        registerPatchInformer(ResourceLocation.withDefaultNamespace("select"));
        registerPatchInformer(ResourceLocation.withDefaultNamespace("condition"));
    }

    @Unique
    private static void registerPatchInformer(ResourceLocation location) {
        RpfModelsCodecsExtends.getInstance().register(
                location,
                ResourceLocation.CODEC.optionalFieldOf("rpt$patch"),
                (o, resourceLocation) -> {
                    if (o instanceof PatchInformer informer && resourceLocation.isPresent()) {
                        informer.rpt$setPatchPath(resourceLocation.get());
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
