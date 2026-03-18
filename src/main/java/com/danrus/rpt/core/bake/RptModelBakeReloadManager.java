package com.danrus.rpt.core.bake;

import com.danrus.rpt.duck.BakingContextSource;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class RptModelBakeReloadManager {
    private final List<RptModelBakeReloadListener> listeners = new ArrayList<>();

    public void add(RptModelBakeReloadListener listener) {
        listeners.add(listener);
    }

    public CompletableFuture<Void> prepare(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.allOf(
                listeners.stream()
                        .map(listener -> listener.prepare(resourceManager, executor))
                        .toArray(CompletableFuture[]::new)
        );
    }

    public CompletableFuture<Void> bake(BakingContextSource source, ModelBaker baker, Executor executor) {
        return CompletableFuture.allOf(
                listeners.stream()
                        .map(listener -> listener.bake(source, baker, executor))
                        .toArray(CompletableFuture[]::new)
        );
    }
}
