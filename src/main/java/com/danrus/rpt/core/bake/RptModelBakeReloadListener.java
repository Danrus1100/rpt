package com.danrus.rpt.core.bake;

import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public interface RptModelBakeReloadListener {
    CompletableFuture<Void> prepare(ResourceManager resourceManager, Executor executor);
    CompletableFuture<Void> bake(Supplier<ItemModel.BakingContext> context, Executor executor);
}
