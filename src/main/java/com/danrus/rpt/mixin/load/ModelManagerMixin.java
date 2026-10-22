package com.danrus.rpt.mixin.load;

import com.danrus.rpt.Rpt;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.resources.model.*;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ModelManager.class)
public class ModelManagerMixin {

    @WrapMethod(
            method = "reload"
    )
    private CompletableFuture<Void> rpt$wrapReload(PreparableReloadListener.SharedState sharedState, Executor executor, PreparableReloadListener.PreparationBarrier preparationBarrier, Executor executor2, Operation<CompletableFuture<Void>> original) {
        ResourceManager resourceManager = sharedState.resourceManager();
        // Template parsing is synchronous, so this completes before model discovery runs.
        // Do not block the reload executor from inside ModelDiscoveryEvent.
        Rpt.getReloadManager().prepare(resourceManager, executor).join();
        return original.call
                (sharedState, executor, preparationBarrier, executor2);
    }
}
