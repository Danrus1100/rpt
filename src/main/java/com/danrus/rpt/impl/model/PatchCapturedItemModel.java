package com.danrus.rpt.impl.model;

import com.danrus.rpf.api.RpfItemModel;
import com.danrus.rpf.api.TestsResultCollector;
import com.danrus.rpf.core.item.ModelUpdateContext;
import com.danrus.rpf.duck.item.RpfBlockModelWrapper;
import com.danrus.rpt.core.OwnerHolder;
import com.danrus.rpt.core.item.RptField;
import com.danrus.rpt.core.template.RptTemplate;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PatchCapturedItemModel extends AbstractRpfItemModel implements RpfBlockModelWrapper {

    private final RptTemplate capture;

    public PatchCapturedItemModel(RptTemplate capture) {
        this.capture = capture;
    }

    @Override
    boolean rpf$doDelegate(ModelUpdateContext context, ItemStack stack, OwnerHolder owner, @Nullable ItemModel prev, TestsResultCollector collector) {
        RptField params = RptField.fromItemStack(stack).merge(capture.params());
        RptField.putToItemStack(stack, params);
        return ((RpfItemModel) capture.model()).rpf$doDelegate(context, stack, owner.get(), this, collector);
    }

    @Override
    void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, OwnerHolder owner, int seed) {
        RptField params = RptField.fromItemStack(stack).merge(capture.params());
        RptField.putToItemStack(stack, params);
        capture.model().update(renderState, stack, itemModelResolver, displayContext, level, owner.get(), seed);
    }

    @Override
    public void rpf$setModelLink(ResourceLocation resourceLocation) {
        if (capture.model() instanceof RpfBlockModelWrapper model) {
            model.rpf$setModelLink(resourceLocation);
        }
    }

    @Override
    public ResourceLocation rpf$getModelLink() {
        if (capture.model() instanceof RpfBlockModelWrapper model) {
            return model.rpf$getModelLink();
        }
        return null;
    }
}
