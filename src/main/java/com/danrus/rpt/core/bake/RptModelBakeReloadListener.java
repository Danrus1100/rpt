package com.danrus.rpt.core.bake;

import com.danrus.rpt.duck.BakingContextSource;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface RptModelBakeReloadListener {
    CompletableFuture<Void> prepare(ResourceManager resourceManager, Executor executor);
    CompletableFuture<Void> bake(BakingContextSource source, ModelBaker baker, Executor executor);
}
