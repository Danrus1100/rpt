package com.danrus.rpt.core.template;

import com.danrus.rpt.core.bake.RptModelBakeReloadListener;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.StrictJsonParser;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TemplatesManager implements RptModelBakeReloadListener {

    private static final FileToIdConverter TEMPLATE_LISTENER = FileToIdConverter.json("rpt/templates");
    private static final Logger log = LoggerFactory.getLogger(TemplatesManager.class);

    private final Map<Identifier, RptTemplate.Unbaked> unbakedTemplates = new HashMap<>();
    private final Map<Identifier, RptTemplate> templates = new HashMap<>();

    private static final Matrix4fc IDENTITY = new Matrix4f();

    @Override
    public CompletableFuture<Void> prepare(ResourceManager resourceManager, Executor executor) {
        // Template parsing is small and must be complete before model discovery runs.
        // Keeping it synchronous also avoids joining a task submitted to the reload executor.
        templates.clear();
        unbakedTemplates.clear();

        try {
            TEMPLATE_LISTENER.listMatchingResources(resourceManager).forEach((location, resource) -> {
                try (var reader = resource.openAsReader()) {
                    RptTemplate.Unbaked unbaked = RptTemplate.Unbaked.CODEC.parse(JsonOps.INSTANCE, StrictJsonParser.parse(reader)).getOrThrow(string -> new RuntimeException("Failed to parse template: " + location + ": " + string));
                    unbakedTemplates.put(TEMPLATE_LISTENER.fileToId(location), unbaked);
                } catch (Exception e) {
                    log.error("Failed to load template: {}", location, e);
                }
            });
        } catch (Exception e) {
            log.error("Failed to list templates", e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> bake(
            Supplier<ItemModel.BakingContext> context,
            Executor executor
            ) {
        return CompletableFuture.runAsync(() ->{
            for (var entry : unbakedTemplates.entrySet()) {
                try {
                    RptTemplate baked = entry.getValue().bake(context.get()
                            , IDENTITY
                    );
                    templates.put(entry.getKey(), baked);
                } catch (Exception e) {
                    log.error("Failed to bake template: {}", entry.getKey(), e);
                }
            }
        }, executor);
    }

    @Nullable
    public RptTemplate tryBakeAgain(
            ItemModel.BakingContext context,
            Identifier location
    ) {
        var a = unbakedTemplates.get(location);
        if (a == null) return null;
        return a.bake(context
                , IDENTITY
        );
    }

    @Nullable
    public RptTemplate getTemplate(ItemModel.BakingContext context, Identifier id) {
        RptTemplate candidate = templates.get(id);
        if (candidate == null || candidate.needRebake()) {
            RptTemplate baked = tryBakeAgain(context, id);
            if (baked != null && !baked.needRebake()) {
                templates.put(id, baked);
            }
            return baked;
        } else {
            return candidate;
        }
    }
    public void forEachUnbakedTemplate(Consumer<ResolvableModel> consumer) {
        unbakedTemplates.values().forEach(unbaked -> consumer.accept(unbaked.unbaked()));
    }

    /**
     * Model ids referenced from template rpt fields are not visible to vanilla dependency
     * discovery, so they have to be registered as roots separately.
     */
    public void forEachReferencedModel(Consumer<Identifier> consumer) {
        unbakedTemplates.values().forEach(unbaked ->
                unbaked.params().variables().models().values().forEach(consumer)
        );
    }
}
