package com.danrus.rpt.impl.model;

import com.danrus.rpf.api.RpfItemModel;
import com.danrus.rpf.api.TestsResultCollector;
import com.danrus.rpf.core.item.ModelUpdateContext;
import com.danrus.rpt.core.OwnerHolder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractRpfItemModel implements RpfItemModel, ItemModel {

    private boolean isFallback = false;


    @Override
    public boolean rpf$doDelegate(ModelUpdateContext context, ItemStack stack,
                                  //? if <=1.21.8 {
                                  @Nullable LivingEntity owner
                                  //? } else {
                                  /*@Nullable net.minecraft.world.entity.ItemOwner owner
                                   *///? }
                                  , @Nullable ItemModel prev, TestsResultCollector collector) {
        return rpf$doDelegate(context, stack, new OwnerHolder(owner), prev, collector);
    }

    abstract boolean rpf$doDelegate(ModelUpdateContext context, ItemStack stack, OwnerHolder owner, @Nullable ItemModel prev, TestsResultCollector collector);

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level,
                       //? if <=1.21.8 {
                        @Nullable LivingEntity owner,
                        //? } else {
                       /*@Nullable net.minecraft.world.entity.ItemOwner owner,
                       *///? }
                       int seed) {
        update(renderState, stack, itemModelResolver, displayContext, level, new OwnerHolder(owner), seed);
    }

    abstract void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, OwnerHolder owner, int seed);

    @Override
    public void rpf$markAsFallback() {
        this.isFallback = true;
    }

    @Override
    public boolean rpf$isFallback() {
        return isFallback;
    }
}
