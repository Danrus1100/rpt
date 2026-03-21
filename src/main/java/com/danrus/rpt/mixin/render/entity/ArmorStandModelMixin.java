package com.danrus.rpt.mixin.render.entity;

import com.danrus.rpt.core.arm.ArmTransform;
import com.danrus.rpt.core.arm.ArmTransformsHelper;
import com.danrus.rpt.duck.CustomArmTransformHolder;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStandModel.class)
public abstract class ArmorStandModelMixin extends HumanoidModel {
    public ArmorStandModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;)V",
            at = @At("TAIL")
    )
    private void rpt$animateArmorStandArms(ArmorStandRenderState renderState, CallbackInfo ci) {
        ArmTransform rightArmTransform = ((CustomArmTransformHolder) ArmTransformsHelper.getRightItem(renderState)).rpt$getRightArmTransform();
        if (!rightArmTransform.isEmpty() && rightArmTransform.getVanillaIfPresent() == null && rightArmTransform.armorStands()) {
            rightArmTransform.rotateModelPart(rightArm, head, true, renderState);
        }

        ArmTransform leftArmTransform = ((CustomArmTransformHolder) ArmTransformsHelper.getLeftItem(renderState)).rpt$getLeftArmTransform();
        if (!leftArmTransform.isEmpty() && leftArmTransform.getVanillaIfPresent() == null && rightArmTransform.armorStands()) {
            leftArmTransform.rotateModelPart(leftArm, head, false, renderState);
        }
    }
}
