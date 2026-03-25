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
    private static final FileToIdConverter FSM_LISTENER = new FileToIdConverter("rpt/bb/fsm", ".json");
    private static final Logger log = LoggerFactory.getLogger(RptBbModelManager.class);
    private final Map<Identifier, BbModelDocument> models = new HashMap<>();
    private final Map<BbModelStateIdentity, FsmInstance> dynamicStates = new HashMap<>();
    private final Map<Identifier, FsmController> controllers = new HashMap<>();


    public @Nullable BbModelDocument getModel(Identifier modelLocation) {
        return models.get(modelLocation);
    }

    @Nullable
    public FsmInstance getDynamicState(Identifier location, int seed, @Nullable LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext) {
        FsmController controller = controllers.get(location);
        if (controller == null) return null;
        BbModelStateIdentity identity = BbModelStateIdentity.of(location, seed);
        if (entity != null) {
            identity.addElement(entity.getUUID());
        }
        identity.addElement(BbStackIdentity.of(stack));
        identity.addElement(displayContext);
        return dynamicStates.computeIfAbsent(identity, location1 -> new FsmInstance(controller));
    }


    public void trigger(String trigger, @Nullable LivingEntity entity) {
        dynamicStates.values().forEach(i -> {
            if (entity == null) {
                i.trigger(trigger, null);
                return;
            }

            LivingEntity captured = i.getCapturedEntity();
            if (captured != null && captured.getUUID().equals(entity.getUUID())) {
                i.trigger(trigger, entity);
            }
        });
    }

    public void triggerForHand(String trigger, @Nullable LivingEntity entity, ItemStack stack, ItemDisplayContext displayContext) {
        if (stack.isEmpty()) {
            return;
        }

        BbStackIdentity stackIdentity = BbStackIdentity.of(stack);
        dynamicStates.forEach((identity, instance) -> {
            if (!identity.containsElement(displayContext) || !identity.containsElement(stackIdentity)) {
                return;
            }

            if (entity == null) {
                instance.trigger(trigger, null);
                return;
            }

            LivingEntity captured = instance.getCapturedEntity();
            if (captured != null && captured.getUUID().equals(entity.getUUID())) {
                instance.trigger(trigger, entity);
            }
        });
    }

    @Override
    public CompletableFuture<Void> prepare(ResourceManager resourceManager, Executor executor) {
        models.clear();
        RptBbModelUtils.clearCaches();
        controllers.clear();
        return CompletableFuture.runAsync(() -> {
            FSM_LISTENER.listMatchingResources(resourceManager).forEach((location, resource) -> {
                try(Reader reader = resource.openAsReader()) {
                    FsmController controller = FsmController.CODEC.parse(JsonOps.INSTANCE, StrictJsonParser.parse(reader)).getOrThrow();
                    controllers.put(FSM_LISTENER.fileToId(location), controller);
                } catch (Exception e) {
                    log.error("Failed to load FSM {} form pack {}", location, resource.sourcePackId(), e);
                }
            });
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
        dynamicStates.clear();
        return CompletableFuture.completedFuture(null);
    }
}
