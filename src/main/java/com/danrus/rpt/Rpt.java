package com.danrus.rpt;

import com.danrus.rpf.Rpf;
import com.danrus.rpf.api.event.AbstractStagedEvent;
import com.danrus.rpf.api.event.type.*;
import com.danrus.rpt.core.bake.RptModelBakeReloadListener;
import com.danrus.rpt.core.bake.RptModelBakeReloadManager;
import com.danrus.rpt.core.fpa.FirstPersonAnimManager;
import com.danrus.rpt.core.item.RptField;
import com.danrus.rpt.core.template.TemplatesManager;
import com.danrus.rpt.core.textures.TextureSwappersManager;
import com.danrus.rpt.duck.*;
import com.danrus.rpt.impl.select.RptSelectItemModelProperty;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class Rpt {

    private static final RptModelBakeReloadManager reloadManager = new RptModelBakeReloadManager();
    private static final TemplatesManager templatesManager = new TemplatesManager();

    private static final TextureSwappersManager swappersManager = new TextureSwappersManager();
    private static final FirstPersonAnimManager fpaManager = new FirstPersonAnimManager();

    public static void prepareModelParams(RptSignedItemModel signedItemModel, RptFieldHolder holder) {
        holder.rpt$clearParams();
        signedItemModel.rpt$getField().ifPresent(params -> {
            holder.rpt$setParams(params);
        });
    }

    /**
     * Applies model-specific fields while a child model is evaluated and restores the
     * previous fields afterwards. Extra fields take precedence over the item's own fields.
     */
    public static <T> T withExtraParams(ItemStack stack, RptField extra, Supplier<T> action) {
        RptField original = RptField.fromItemStack(stack);
        if (extra == null || extra.equals(RptField.EMPTY)) {
            return action.get();
        }

        RptField.putToItemStack(stack, original.merge(extra));
        try {
            return action.get();
        } finally {
            RptField.putToItemStack(stack, original);
        }
    }

    public static void init() {
        reloadManager.add(templatesManager);

        Rpf.getEventBus().register(UpdateModelEvent.class, event -> {
            RptSignedItemModel signedItemModel = RptSignedItemModel.class.cast(event.getModel());
            RptFieldHolder holder = RptFieldHolder.class.cast(event.getStack());
            prepareModelParams(signedItemModel, holder);
        });

        Rpf.getEventBus().register(SelectModelPropertyGetWhenDoDelegateEvent.class, event -> {
            SelectItemModelProperty property = event.getProperty();
            if (property instanceof RptSelectItemModelProperty rptProperty) {
                RptField params = RptField.fromItemStack(event.getStack())
                        .merge(RptSelectItemModel.class.cast(event.getModel()).rpt$getField());
                LivingEntity owner = event.getOwner() == null ? null : event.getOwner().asLivingEntity();
                event.setGetter(() -> rptProperty.get(
                        event.getStack(), event.getContext().level(), owner,
                        event.getContext().seed(), event.getContext().displayContext(), params
                ));
            }
        });

        Rpf.getEventBus().register(ModelDiscoveryEvent.class, event -> {
            if (event.getStage() == AbstractStagedEvent.Stage.PRE) {
                Rpt.getTemplatesManager().forEachUnbakedTemplate(event.getModelDiscovery()::addRoot);
            }
        });

        Rpf.getEventBus().register(PreBakeEvent.class, event -> {
            RptBakingContext.class.cast(event.getBakingContext()).rpt$addFields(RptClientItem.class.cast(event.getClientItem()).rpt$getField().orElse(RptField.EMPTY));
        });

        Rpf.getEventBus().register(PostBakeEvent.class, event -> {
            RptSignedItemModel signed = RptSignedItemModel.class.cast(event.getResult());
            RptClientItem clientItem = RptClientItem.class.cast(event.getClientItem());
            clientItem.rpt$getField().ifPresent(params -> {
                signed.rpt$setField(params);
            });
        });
    }

    public static RptModelBakeReloadManager getReloadManager() { return reloadManager; }

    public static TemplatesManager getTemplatesManager() {
        return templatesManager;
    }

    public static TextureSwappersManager getTextureSwappersManager() {return swappersManager;}

    public static FirstPersonAnimManager getFpaManager() {
        return fpaManager;
    }
}
