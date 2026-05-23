package com.danrus.rpt.mixin.item.select;

import com.danrus.rpt.duck.RptBakingContext;
import com.danrus.rpt.duck.RptSelectItemModel;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SelectItemModel.Unbaked.class)
public class UnbakedMixin {
    @WrapOperation(
            method = "bake",
            at = @At(value = "INVOKE", target =
                    //? <26.1
                    "Lnet/minecraft/client/renderer/item/SelectItemModel$UnbakedSwitch;bake(Lnet/minecraft/client/renderer/item/ItemModel$BakingContext;Lnet/minecraft/client/renderer/item/ItemModel;)Lnet/minecraft/client/renderer/item/ItemModel;"
                    //? >=26.1
                    //"Lnet/minecraft/client/renderer/item/SelectItemModel$UnbakedSwitch;bake(Lnet/minecraft/client/renderer/item/ItemModel$BakingContext;Lorg/joml/Matrix4fc;Lnet/minecraft/client/renderer/item/ItemModel;)Lnet/minecraft/client/renderer/item/ItemModel;"
            )
    )
    private ItemModel rpt$wrapSelectModel
        //? <26.1
        (SelectItemModel.UnbakedSwitch instance, ItemModel.BakingContext bakingContext, ItemModel itemModel, Operation<ItemModel> original)
        //? >= 26.1
        //(SelectItemModel.UnbakedSwitch instance, ItemModel.BakingContext bakingContext, Matrix4fc transformations, ItemModel itemModel, Operation<ItemModel> original)
    {
        ItemModel originalModel = original.call(instance, bakingContext,
                //? >=26.1
                //transformations,
                itemModel);
        if (originalModel instanceof SelectItemModel selectItemModel) {
            RptSelectItemModel.class.cast(selectItemModel).rpt$setField(RptBakingContext.class.cast(bakingContext).rpt$getField());
        }
        return originalModel;
    }

}
