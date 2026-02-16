package com.danrus.rpt.mixin.load;

import com.danrus.rpt.Rpt;
import com.danrus.rpt.duck.BakingContextSource;
import com.danrus.rpt.duck.ModelBakerSource;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ModelManager.class)
public class ModelManagerMixin {

    @WrapMethod(
            method = "reload"
    )
    private CompletableFuture<Void> rpt$wrapReload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, Executor executor, Executor executor2, Operation<CompletableFuture<Void>> original) {
        Rpt.rpt$repairFuture = Rpt.getTemplatesManager().prepare(resourceManager, executor);
        return original.call(preparationBarrier, resourceManager, executor, executor2);
    }

//    @Inject(
//            method = "discoverModelDependencies",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelDiscovery;addSpecialModel(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/resources/model/UnbakedModel;)V")
//    )
//    private static void rpt$injectDiscoverModelDependencies(Map<ResourceLocation, UnbakedModel> inputModels, BlockStateModelLoader.LoadedModels loadedModels, ClientItemInfoLoader.LoadedClientInfos loadedClientInfos, CallbackInfoReturnable<ModelManager.ResolvedModels> cir, @Local ModelDiscovery modelDiscovery) {
//        // FIXME: move to events
//
//    }


    @WrapOperation(
            method = "loadModels",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBakery;bakeModels(Lnet/minecraft/client/resources/model/SpriteGetter;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;")
    )
    private static CompletableFuture<ModelBakery.BakingResult> rpt$wrapLoadModels(ModelBakery instance, SpriteGetter sprites, Executor executor, Operation<CompletableFuture<ModelBakery.BakingResult>> original) {
        return Rpt.getTemplatesManager().bake(
                (BakingContextSource) instance,
                ((ModelBakerSource)instance).rpt$createModelBaker(sprites),
                executor
        ).thenCompose(v -> original.call(instance, sprites, executor));
    }
}
