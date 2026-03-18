package com.danrus.rpt.impl.model;

import com.danrus.rpf.api.RpfItemModel;
import com.danrus.rpf.api.TestsResultCollector;
import com.danrus.rpf.core.item.ModelUpdateContext;
import com.danrus.rpt.core.OwnerHolder;
import com.danrus.rpt.core.item.RptItemParams;
import com.danrus.rpt.core.template.RptTemplate;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PatchCapturedItemModel extends AbstractRpfItemModel {

    private final RptTemplate capture;

    public PatchCapturedItemModel(RptTemplate capture) {
        this.capture = capture;
    }

    @Override
    boolean rpf$doDelegate(ModelUpdateContext context, ItemStack stack, OwnerHolder owner, @Nullable ItemModel prev, TestsResultCollector collector) {
        RptItemParams params = RptItemParams.fromItemStack(stack).merge(capture.params());
        RptItemParams.putToItemStack(stack, params);
        return ((RpfItemModel) capture.model()).rpf$doDelegate(context, stack, owner.get(), this, collector);
    }

    @Override
    void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, OwnerHolder owner, int seed) {
        RptItemParams params = RptItemParams.fromItemStack(stack).merge(capture.params());
        RptItemParams.putToItemStack(stack, params);
        capture.model().update(renderState, stack, itemModelResolver, displayContext, level, owner.get(), seed);
    }
}
