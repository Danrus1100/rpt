package com.danrus.rpt.mixin.load;

import com.danrus.rpt.Rpt;
import com.danrus.rpt.duck.BakingContextSource;
import com.danrus.rpt.duck.ModelBakerSource;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.resources.model.SpriteGetter;
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
import java.util.function.Supplier;

@Mixin(ModelManager.class)
public class ModelManagerMixin {

    @WrapMethod(
            method = "reload"
    )
    //? if <=1.21.8 {
    private CompletableFuture<Void> rpt$wrapReload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, Executor executor, Executor executor2, Operation<CompletableFuture<Void>> original) {
    //? } else {
    /*private CompletableFuture<Void> rpt$wrapReload(PreparableReloadListener.SharedState sharedState, Executor executor, PreparableReloadListener.PreparationBarrier preparationBarrier, Executor executor2, Operation<CompletableFuture<Void>> original) {
        ResourceManager resourceManager = sharedState.resourceManager();
    *///?}
        Rpt.rpt$repairFuture = Rpt.getReloadManager().prepare(resourceManager, executor);
        return original.call
                //? if <=1.21.8 {
                (preparationBarrier, resourceManager, executor, executor2);
                //? } else {
                /*(sharedState, executor, preparationBarrier, executor2);
                *///?}
    }

    //? <26.1 {
    @WrapOperation(
            method = "loadModels",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBakery;bakeModels(Lnet/minecraft/client/resources/model/SpriteGetter;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;")
    )
    private static CompletableFuture<ModelBakery.BakingResult> rpt$wrapLoadModels(ModelBakery instance, SpriteGetter sprites, Executor executor, Operation<CompletableFuture<ModelBakery.BakingResult>> original) {
    //?} else {
    /*@WrapOperation(
            method = "loadModels",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBakery;bakeModels(Lnet/minecraft/client/resources/model/sprite/MaterialBaker;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;")
    )
    private static CompletableFuture<ModelBakery.BakingResult> rpt$wrapLoadModels(ModelBakery instance, net.minecraft.client.resources.model.sprite.MaterialBaker sprites, Executor executor, Operation<CompletableFuture<ModelBakery.BakingResult>> original) {
    *///?}
        BakingContextSource contextSource = (BakingContextSource) instance;
        ModelBakerSource bakerSource = (ModelBakerSource) instance;
        Supplier<ItemModel.BakingContext> source = () -> contextSource.rpt$createBakingContext(bakerSource.rpt$createModelBaker(sprites));

        return Rpt.getReloadManager().bake(
                source,
                executor
        ).thenCompose(v -> original.call(instance, sprites, executor));
    }
}
