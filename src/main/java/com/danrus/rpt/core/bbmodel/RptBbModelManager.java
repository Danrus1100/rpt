package com.danrus.rpt.core.bbmodel;

import com.danrus.bb4j.api.BbModel;
import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.rpt.core.bake.RptModelBakeReloadListener;
import com.danrus.rpt.duck.BakingContextSource;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class RptBbModelManager implements RptModelBakeReloadListener {

    private static final FileToIdConverter LISTENER = new FileToIdConverter("rpt/bb", ".bbmodel");
    private static final Logger log = LoggerFactory.getLogger(RptBbModelManager.class);
    private Map<ResourceLocation, BbModelDocument> models = new HashMap<>();
    private Map<ResourceLocation, BbModelDynamicState> dynamicStates = new HashMap<>();


    public @Nullable BbModelDocument getModel(ResourceLocation modelLocation) {
        return models.get(LISTENER.idToFile(modelLocation));
    }

    public BbModelDynamicState getDynamicState(ResourceLocation location) {
        return dynamicStates.computeIfAbsent(location, location1 -> new BbModelDynamicState());
    }

    @Override
    public CompletableFuture<Void> prepare(ResourceManager resourceManager, Executor executor) {
        models.clear();
        dynamicStates.clear();
        return CompletableFuture.runAsync(() -> {
            LISTENER.listMatchingResources(resourceManager).forEach((resourceLocation, resource) ->  {
                try (Reader reader = resource.openAsReader()) {
                    BbModelDocument document = BbModel.read(reader);
                    RptBbModel.registerModelTextures(document);
                    models.put(resourceLocation, document);
                } catch (IOException e) {
                    log.error("Failed to load bbmodel {} from pack {}: ", resourceLocation, resource.sourcePackId(), e);
                }
            });
        });
    }

    @Override
    public CompletableFuture<Void> bake(BakingContextSource source, ModelBaker baker, Executor executor) {
        return CompletableFuture.completedFuture(null);
    }
}
