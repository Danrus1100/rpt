package com.danrus.rpt.impl.model;

import com.danrus.rpt.core.item.RptItemParams;
import com.danrus.rpt.impl.select.RptSelectItemModelProperty;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class RptSelectItemModel<T> extends SelectItemModel<T> {

    private final RptItemParams params;

    public RptSelectItemModel(SelectItemModelProperty<T> selectItemModelProperty, ModelSelector<T> modelSelector, RptItemParams params) {
        super(selectItemModelProperty, modelSelector);
        this.params = params;
    }

    @Override
    public void update(ItemStackRenderState itemStackRenderState, ItemStack itemStack, ItemModelResolver itemModelResolver, ItemDisplayContext itemDisplayContext, @Nullable ClientLevel clientLevel,
                       //? if <=1.21.8 {
                       /*@Nullable LivingEntity owner,
                       *///? } else {
                       net.minecraft.world.entity.ItemOwner owner,
                       //? }
                       int i) {
        if (property instanceof RptSelectItemModelProperty<T>) {

        }
        super.update(itemStackRenderState, itemStack, itemModelResolver, itemDisplayContext, clientLevel, owner, i);
    }
}
