package com.danrus.rpt;

import com.danrus.rpf.Rpf;
import com.danrus.rpf.api.event.AbstractStagedEvent;
import com.danrus.rpf.api.event.type.ModelDiscoveryEvent;
import com.danrus.rpf.api.event.type.PostBakeEvent;
import com.danrus.rpf.api.event.type.PreBakeEvent;
import com.danrus.rpf.api.event.type.UpdateModelEvent;
import com.danrus.rpt.core.RptItemParams;
import com.danrus.rpt.duck.RptBakingContext;
import com.danrus.rpt.duck.RptClientItem;
import com.danrus.rpt.duck.RptItemParamsHolder;
import com.danrus.rpt.duck.RptSignedItemModel;
import net.fabricmc.api.ClientModInitializer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class Rpt implements ClientModInitializer {

    private static final RptTemplatesManager templatesManager = new RptTemplatesManager();
    private static final Logger log = LoggerFactory.getLogger(Rpt.class);
    @Nullable
    public static CompletableFuture<Void> rpt$repairFuture = null;

    @Override
    public void onInitializeClient() {
        Rpf.getEventBus().register(UpdateModelEvent.class, event -> {
            RptSignedItemModel signedItemModel = RptSignedItemModel.class.cast(event.getModel());
                signedItemModel.rpt$getParams().ifPresent(params -> {
                    RptItemParamsHolder.class.cast(event.getStack()).rpt$setParams(params);
                });
        });

        Rpf.getEventBus().register(ModelDiscoveryEvent.class, event -> {
            if (event.getStage() == AbstractStagedEvent.Stage.PRE) {
                if (Rpt.rpt$repairFuture == null) {
                    log.error("Templates were not prepared in time for model discovery. it shouldn't happen!");
                    return;
                }
                Rpt.rpt$repairFuture.join();
                Rpt.rpt$repairFuture = null;
                Rpt.getTemplatesManager().forEachUnbakedTemplate(event.getModelDiscovery()::addRoot);
            }
        });

        Rpf.getEventBus().register(PreBakeEvent.class, event -> {
            RptBakingContext.class.cast(event.getBakingContext()).rpt$setParams(RptClientItem.class.cast(event.getClientItem()).rpt$getParams().orElse(RptItemParams.EMPTY));
        });

        Rpf.getEventBus().register(PostBakeEvent.class, event -> {
            RptSignedItemModel signed = RptSignedItemModel.class.cast(event.getResult());
            RptClientItem clientItem = RptClientItem.class.cast(event.getClientItem());
            clientItem.rpt$getParams().ifPresent(params -> {
                signed.rpt$setParams(params);
            });
        });
    }

    public static RptTemplatesManager getTemplatesManager() {
        return templatesManager;
    }
}
