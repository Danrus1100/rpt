package com.danrus.rpt.mixin.rpf;

import com.danrus.rpf.api.RpfItemModel;
import com.danrus.rpf.api.TestsResultCollector;
import com.danrus.rpf.core.item.ModelUpdateContext;
import com.danrus.rpf.core.item.SignedItemModel;
import com.danrus.rpt.Rpt;
import com.danrus.rpt.core.item.RptField;
import com.danrus.rpt.duck.RptFieldHolder;
import com.danrus.rpt.duck.RptSignedItemModel;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(SignedItemModel.class)
public class SignedItemModelMixin implements RptSignedItemModel {

    @Unique
    @Nullable
    private RptField rpt$params;

    @Override
    public Optional<RptField> rpt$getField() {
        return Optional.ofNullable(this.rpt$params);
    }

    @Override
    public void rpt$setField(@Nullable RptField params) {
        this.rpt$params = params;
    }


    @WrapOperation(
            method = "doDelegate",
            at = @At(value = "INVOKE", target =
                    //? >= 1.21.10 {
                    "Lcom/danrus/rpf/api/RpfItemModel;rpf$doDelegate(Lcom/danrus/rpf/core/item/ModelUpdateContext;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/ItemOwner;Lnet/minecraft/client/renderer/item/ItemModel;Lcom/danrus/rpf/api/TestsResultCollector;)Z"
                    //? } else {
                    /*"Lcom/danrus/rpf/api/RpfItemModel;rpf$doDelegate(Lcom/danrus/rpf/core/item/ModelUpdateContext;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/item/ItemModel;Lcom/danrus/rpf/api/TestsResultCollector;)Z"
                    *///? }
            )
    )
    private boolean rpt$wrapDelegate(RpfItemModel instance, ModelUpdateContext context, ItemStack stack,
                                     //? >=1.21.10 {
                                     net.minecraft.world.entity.ItemOwner owner,
                                     //? } else {
                                     /*LivingEntity owner,
                                     *///?}
                                     ItemModel prev, TestsResultCollector collector, Operation<Boolean> original) {
        RptSignedItemModel signedItemModel = RptSignedItemModel.class.cast(this);
        RptFieldHolder holder = RptFieldHolder.class.cast(stack);
        Rpt.prepareModelParams(signedItemModel, holder);
        boolean result = original.call(instance, context, stack, owner, prev, collector);
        holder.rpt$clearParams();
        return result;
    }
}
