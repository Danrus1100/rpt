package com.danrus.rpt.impl.model;

import com.danrus.rpf.api.RpfItemModel;
import com.danrus.rpf.logging.ModelTestsResultCollector;
import com.danrus.rpt.core.OwnerHolder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractRpfItemModel implements RpfItemModel, ItemModel {

    private boolean isFallback = false;

    public boolean rpf$doDelegate(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level,
                                 //? if <=1.21.8 {
                                 /*@Nullable LivingEntity owner,
                                 *///? } else {
                                 @Nullable net.minecraft.world.entity.ItemOwner owner,
                                 //? }
                                 @Nullable ItemModel prev, int seed, Identifier itemModelId, String packName, ModelTestsResultCollector collector) {
        return rpf$doDelegate(renderState, stack, itemModelResolver, displayContext, level, new OwnerHolder(owner), prev, seed, itemModelId, packName, collector);
    }

    abstract boolean rpf$doDelegate(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, OwnerHolder owner, @Nullable ItemModel prev, int seed, Identifier itemModelId, String packName, ModelTestsResultCollector collector);

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level,
                       //? if <=1.21.8 {
                        /*@Nullable LivingEntity owner,
                        *///? } else {
                       @Nullable net.minecraft.world.entity.ItemOwner owner,
                       //? }
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
