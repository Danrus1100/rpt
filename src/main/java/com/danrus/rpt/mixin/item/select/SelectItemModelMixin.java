package com.danrus.rpt.mixin.item.select;

import com.danrus.rpt.core.item.RptItemParams;
import com.danrus.rpt.duck.RptItemParamsHolder;
import com.danrus.rpt.duck.RptSelectItemModel;
import com.danrus.rpt.impl.select.RptSelectItemModelProperty;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SelectItemModel.class)
public class SelectItemModelMixin<T> implements RptSelectItemModel {

    @Unique
    private RptItemParams rpt$params;

    @Override
    public void rpt$setParams(RptItemParams params) {
        this.rpt$params = params;
    }

    @Override
    public RptItemParams rpt$getParams() {
        return rpt$params;
    }

    @WrapOperation(
            method = "update",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/properties/select/SelectItemModelProperty;get(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/world/entity/LivingEntity;ILnet/minecraft/world/item/ItemDisplayContext;)Ljava/lang/Object;")
    )
    private T rpt$wrapGet(SelectItemModelProperty<T> instance, ItemStack stack, ClientLevel level, LivingEntity entity, int i, ItemDisplayContext itemDisplayContext, Operation<T> original) {
        if (instance instanceof RptSelectItemModelProperty<T> rptProperty) {
            RptItemParams params = RptItemParamsHolder.class.cast(stack).rpt$getParams().orElse(rpt$params);
            return rptProperty.get(stack, level, entity, i, itemDisplayContext, rpt$params.merge(params));
        }
        return original.call(instance, stack, level, entity, i, itemDisplayContext);
    }
}
