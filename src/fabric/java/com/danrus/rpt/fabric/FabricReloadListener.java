package com.danrus.rpt.fabric;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

final class FabricReloadListener implements IdentifiableResourceReloadListener {

    private final Identifier id;
    private final PreparableReloadListener delegate;

    FabricReloadListener(Identifier id, PreparableReloadListener delegate) {
        this.id = id;
        this.delegate = delegate;
    }

    @Override
    public Identifier getFabricId() {
        return id;
    }

    @Override
    public CompletableFuture<Void> reload(
            SharedState sharedState,
            Executor prepareExecutor,
            PreparationBarrier preparationBarrier,
            Executor applyExecutor
    ) {
        return delegate.reload(sharedState, prepareExecutor, preparationBarrier, applyExecutor);
    }
}
