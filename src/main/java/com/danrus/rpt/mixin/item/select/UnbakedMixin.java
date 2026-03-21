package com.danrus.rpt.mixin.item.select;

import com.danrus.rpt.duck.RptBakingContext;
import com.danrus.rpt.duck.RptSelectItemModel;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SelectItemModel.Unbaked.class)
public class UnbakedMixin {
    @WrapOperation(
            method = "bake",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/SelectItemModel$UnbakedSwitch;bake(Lnet/minecraft/client/renderer/item/ItemModel$BakingContext;Lnet/minecraft/client/renderer/item/ItemModel;)Lnet/minecraft/client/renderer/item/ItemModel;")
    )
    private ItemModel rpt$wrapSelectModel(SelectItemModel.UnbakedSwitch instance, ItemModel.BakingContext bakingContext, ItemModel itemModel, Operation<ItemModel> original) {
        ItemModel originalModel = original.call(instance, bakingContext, itemModel);
        if (originalModel instanceof SelectItemModel selectItemModel) {
            RptSelectItemModel.class.cast(selectItemModel).rpt$setField(RptBakingContext.class.cast(bakingContext).rpt$getField());
        }
        return originalModel;
    }

}
