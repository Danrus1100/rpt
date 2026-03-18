package com.danrus.rpt.core.template;

import com.danrus.rpt.duck.BakingContextSource;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.StrictJsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class RptTemplatesManager {

    private static final FileToIdConverter TEMPLATE_LISTENER = FileToIdConverter.json("rpt/templates");
    private static final Logger log = LoggerFactory.getLogger(RptTemplatesManager.class);

    private final Map<ResourceLocation, RptTemplate.Unbaked> unbakedTemplates = new HashMap<>();
    private final Map<ResourceLocation, RptTemplate> templates = new HashMap<>();

    public CompletableFuture<Void> prepare(ResourceManager resourceManager, Executor executor) {
        templates.clear();
        return CompletableFuture.runAsync(() -> {
            TEMPLATE_LISTENER.listMatchingResources(resourceManager).forEach((location, resource) -> {
                try (var reader = resource.openAsReader()) {
                    RptTemplate.Unbaked unbaked = RptTemplate.Unbaked.CODEC.parse(JsonOps.INSTANCE, StrictJsonParser.parse(reader)).getOrThrow(string -> new RuntimeException("Failed to parse template: " + location + ": " + string));
                    unbakedTemplates.put(TEMPLATE_LISTENER.fileToId(location), unbaked);
                } catch (Exception e) {
                    log.error("Failed to load template: {}", location, e);
                }
            });
        }, executor);
    }

    public CompletableFuture<Void> bake(
            BakingContextSource source,
            ModelBaker baker,
            Executor executor
            ) {
        return CompletableFuture.runAsync(() ->{
            for (var entry : unbakedTemplates.entrySet()) {
                try {
                    ItemModel.BakingContext bakingContext = source.rpt$createBakingContext(baker);
                    RptTemplate baked = entry.getValue().bake(bakingContext);
                    templates.put(entry.getKey(), baked);
                } catch (Exception e) {
                    log.error("Failed to bake template: {}", entry.getKey(), e);
                }
            }
        }, executor);
    }

    public RptTemplate tryBakeAgain(
            ItemModel.BakingContext context,
            ResourceLocation location
    ) {
        return unbakedTemplates.get(location).bake(context);
    }

    public RptTemplate getTemplate(ItemModel.BakingContext context, ResourceLocation id) {
        RptTemplate candidate = templates.get(id);
        if (candidate == null || candidate.needRebake()) {
            return tryBakeAgain(context, id);
        } else {
            return candidate;
        }
    }
    public void forEachUnbakedTemplate(Consumer<ResolvableModel> consumer) {
        unbakedTemplates.values().forEach(unbaked -> consumer.accept(unbaked.unbaked()));
    }
}
