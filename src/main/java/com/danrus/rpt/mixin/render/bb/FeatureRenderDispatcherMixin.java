package com.danrus.rpt.mixin.render.bb;

import com.danrus.rpt.core.bbmodel.nodes.BbModelFeatureRenderer;
import com.danrus.rpt.core.bbmodel.nodes.BbModelsSubmitsCollector;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.renderer.feature.FeatureRenderDispatcher.class)
public class FeatureRenderDispatcherMixin {

    @Shadow
    @Final
    private MultiBufferSource.BufferSource bufferSource;

    @Unique
    private final BbModelFeatureRenderer rpt$bbModelFeatureRenderer = new BbModelFeatureRenderer();

    @Inject(
            method = "renderAllFeatures",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/ItemFeatureRenderer;render(Lnet/minecraft/client/renderer/SubmitNodeCollection;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/OutlineBufferSource;)V")
    )
    private void rpt$renderBbModels(CallbackInfo ci, @Local SubmitNodeCollection submitNodeCollection) {
        rpt$bbModelFeatureRenderer.render((BbModelsSubmitsCollector) submitNodeCollection, bufferSource);
    }
}
