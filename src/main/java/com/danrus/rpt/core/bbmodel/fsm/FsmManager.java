package com.danrus.rpt.core.bbmodel.fsm;

import com.danrus.bb4j.api.BbModel;
import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.rpt.core.bake.RptModelBakeReloadListener;
import com.danrus.rpt.core.bbmodel.BbModelStateIdentity;
import com.danrus.rpt.core.bbmodel.RptBbModelUtils;
import com.danrus.rpt.duck.BakingContextSource;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

public class FsmManager implements RptModelBakeReloadListener {
    private static final FileToIdConverter FSM_LISTENER = new FileToIdConverter("rpt/bb/fsm", ".json");
    private static final Logger log = LoggerFactory.getLogger(FsmManager.class);
    private final Map<BbModelStateIdentity, FsmTickContext> toTick = new HashMap<>();
    private final Map<BbModelStateIdentity, FsmInstance>  instanceMap = new HashMap<>();
    private final Map<Integer, Set<String>> activeTriggers = new HashMap<>();
    private final Map<Identifier, FsmController> controllers = new HashMap<>();

    private final Function<Identifier, @Nullable BbModelDocument> modelGetter;

    public FsmManager(Function<Identifier, @Nullable BbModelDocument> modelGetter) {
        this.modelGetter = modelGetter;
    }

    public void trigger(String trigger, final Entity entity) {
        int seed = entity.getId();
        trigger(trigger, seed);
    }

    public void trigger(String trigger, ItemDisplayContext displayContext, final LivingEntity entity) {
        int seed = entity.getId() + displayContext.ordinal();
        trigger(trigger, seed);
    }

    public void trigger(String trigger, int seed) {
        activeTriggers.computeIfAbsent(seed, s -> new HashSet<>()).add(trigger);
    }

    @Nullable
    public FsmInstance queue(BbModelStateIdentity location, FsmTickContext context) {
        toTick.put(location, context);
        if (!instanceMap.containsKey(location)) {
            FsmController controller = controllers.get(location.modelLocation());
            if (controller == null) return null;
            FsmInstance instance = new FsmInstance(controller);
            instanceMap.put(location, instance);
            return instance;
        }
        return instanceMap.get(location);
    }

    public void tick(DeltaTracker tracker, @Nullable ClientLevel level) {
        final float deltaSeconds = tracker.getGameTimeDeltaTicks() / 20.0f;

        toTick.forEach( (id, ctx)-> {
            try {
                instanceMap.get(id).tick(
                        deltaSeconds,
                        ctx.context(),
                        ctx.stack(),
                        ctx.level(),
                        ctx.entity(),
                        ctx.seed(),
                        ctx.model(),
                        activeTriggers.get(id.seed()) != null
                                ? new HashSet<>( activeTriggers.get(id.seed()) )
                                : new HashSet<>()
                );
            } catch (Exception ignored) {

            }
        });
        activeTriggers.clear();
        toTick.clear();
    }

    @Override
    public CompletableFuture<Void> prepare(ResourceManager resourceManager, Executor executor) {
        controllers.clear();
        activeTriggers.clear();
        return CompletableFuture.runAsync(() -> {
            FSM_LISTENER.listMatchingResources(resourceManager).forEach((location, resource) -> {
                try(Reader reader = resource.openAsReader()) {
                    FsmController controller = FsmController.CODEC.parse(JsonOps.INSTANCE, StrictJsonParser.parse(reader)).getOrThrow();
                    controllers.put(FSM_LISTENER.fileToId(location), controller);
                } catch (Exception e) {
                    log.error("Failed to load FSM {} form pack {}", location, resource.sourcePackId(), e);
                }
            });
        });
    }

    @Override
    public CompletableFuture<Void> bake(BakingContextSource source, ModelBaker baker, Executor executor) {
        instanceMap.clear();
        return CompletableFuture.completedFuture(null);
    }
}
