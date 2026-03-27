package com.danrus.rpt.core.bbmodel;

import com.danrus.bb4j.api.BbModel;
import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.rpt.core.bake.RptModelBakeReloadListener;
import com.danrus.rpt.core.bbmodel.fsm.FsmController;
import com.danrus.rpt.core.bbmodel.fsm.FsmInstance;
import com.danrus.rpt.duck.BakingContextSource;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
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

    private static final FileToIdConverter MODELS_LISTENER = new FileToIdConverter("rpt/bb/models", ".bbmodel");
    private static final Logger log = LoggerFactory.getLogger(RptBbModelManager.class);
    private final Map<Identifier, BbModelDocument> models = new HashMap<>();


    public @Nullable BbModelDocument getModel(Identifier modelLocation) {
        return models.get(modelLocation);
    }

    @Override
    public CompletableFuture<Void> prepare(ResourceManager resourceManager, Executor executor) {
        models.clear();
        RptBbModelUtils.clearCaches();
        return CompletableFuture.runAsync(() -> {
            MODELS_LISTENER.listMatchingResources(resourceManager).forEach((resourceLocation, resource) ->  {
                try (Reader reader = resource.openAsReader()) {
                    BbModelDocument document = BbModel.read(reader);
                    RptBbModelUtils.registerModelTextures(document);
                    models.put(MODELS_LISTENER.fileToId(resourceLocation), document);
                } catch (IOException e) {
                    log.error("Failed to load bbmodel {} from pack {}", resourceLocation, resource.sourcePackId(), e);
                }
            });
        });
    }

    @Override
    public CompletableFuture<Void> bake(BakingContextSource source, ModelBaker baker, Executor executor) {
        return CompletableFuture.completedFuture(null);
    }
}
