package com.danrus.rpt;

import com.danrus.rpf.Rpf;
import com.danrus.rpf.api.event.AbstractStagedEvent;
import com.danrus.rpf.api.event.type.*;
import com.danrus.rpt.core.item.RptField;
import com.danrus.rpt.core.template.TemplatesManager;
import com.danrus.rpt.core.textures.TextureSwappersManager;
import com.danrus.rpt.duck.*;
import com.danrus.rpt.impl.select.RptSelectItemModelProperty;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class Rpt implements ClientModInitializer {

    private static final TemplatesManager templatesManager = new TemplatesManager();
    private static final TextureSwappersManager swappersManager = new TextureSwappersManager();
    private static final Logger log = LoggerFactory.getLogger(Rpt.class);
    @Nullable
    public static CompletableFuture<Void> rpt$repairFuture = null;

    public static void prepareModelParams(RptSignedItemModel signedItemModel, RptItemParamsHolder holder) {
        holder.rpt$clearParams();
        signedItemModel.rpt$getParams().ifPresent(params -> {
            holder.rpt$setParams(params);
        });
    }

    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(swappersManager);

        Rpf.getEventBus().register(UpdateModelEvent.class, event -> {
            RptSignedItemModel signedItemModel = RptSignedItemModel.class.cast(event.getModel());
            RptItemParamsHolder holder = RptItemParamsHolder.class.cast(event.getStack());
            prepareModelParams(signedItemModel, holder);
        });

        Rpf.getEventBus().register(SelectModelPropertyGetWhenDoDelegateEvent.class, event -> {
            SelectItemModelProperty property = event.getProperty();
            if (property instanceof RptSelectItemModelProperty rptProperty) {
                RptField params = RptItemParamsHolder.class.cast(event.getStack()).rpt$getParams().orElse(RptSelectItemModel.class.cast(event.getModel()).rpt$getParams());
                event.setGetter(() -> rptProperty.get(
                        event.getStack(), event.getContext().level(), event.getOwner()
                        //? if >=1.21.10
                        //.asLivingEntity()
                        , event.getContext().seed(), event.getContext().displayContext(), params
                ));
            }
        });

        Rpf.getEventBus().register(ModelDiscoveryEvent.class, event -> {
            if (event.getStage() == AbstractStagedEvent.Stage.PRE) {
                if (Rpt.rpt$repairFuture == null) {
                    log.error("Templates were not prepared in time for value discovery. it shouldn't happen!");
                    return;
                }
                Rpt.rpt$repairFuture.join();
                Rpt.rpt$repairFuture = null;
                Rpt.getTemplatesManager().forEachUnbakedTemplate(event.getModelDiscovery()::addRoot);
            }
        });

        Rpf.getEventBus().register(PreBakeEvent.class, event -> {
            RptBakingContext.class.cast(event.getBakingContext()).rpt$addParams(RptClientItem.class.cast(event.getClientItem()).rpt$getParams().orElse(RptField.EMPTY));
        });

        Rpf.getEventBus().register(PostBakeEvent.class, event -> {
            RptSignedItemModel signed = RptSignedItemModel.class.cast(event.getResult());
            RptClientItem clientItem = RptClientItem.class.cast(event.getClientItem());
            clientItem.rpt$getParams().ifPresent(params -> {
                signed.rpt$setParams(params);
            });
        });
    }

    public static TemplatesManager getTemplatesManager() {
        return templatesManager;
    }

    public static TextureSwappersManager getTextureSwappersManager() {return swappersManager;}
}
