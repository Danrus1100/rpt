package com.danrus.rpt.mixin.render.entity;

import com.danrus.rpt.core.anchor.AnchorType;
import com.danrus.rpt.duck.CustomAnchorApplicableModel;
import com.danrus.rpt.duck.RptItemRenderState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin <T extends EntityRenderState, S extends ArmedEntityRenderState, M extends EntityModel<S> & ArmedModel> extends RenderLayer<S, M> {
    public ItemInHandLayerMixin(RenderLayerParent<S, M> renderer) {
        super(renderer);
    }

    @WrapOperation(
            //? <=1.21.8
            //method = "renderArmWithItem",
            //? >=1.21.10
            method = "submitArmWithItem",
            at = @At(value = "INVOKE", target =
                    //? <1.21.10
                    //"Lnet/minecraft/client/model/ArmedModel;translateToHand(Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V"
                    //? >=1.21.10
                    "Lnet/minecraft/client/model/ArmedModel;translateToHand(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V"
            )
    )
    private void rpt$moveToAnchorPart(ArmedModel instance
            //? >=1.21.10
            , T t
            , HumanoidArm arm, PoseStack poseStack, Operation<Void> original, @Local(argsOnly = true) ItemStackRenderState itemStackRenderState) {
        AnchorType anchorType = ((RptItemRenderState)itemStackRenderState).rpt$getAnchorType();
        if (anchorType == null || !(getParentModel() instanceof CustomAnchorApplicableModel model)) {
            original.call(instance
                    //? >=1.21.10
                    , t
                    , arm, poseStack);
            return;
        }

        model.rpt$poseToAnchor(anchorType, poseStack);
    }
}
