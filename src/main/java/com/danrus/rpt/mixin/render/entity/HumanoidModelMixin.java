package com.danrus.rpt.mixin.render.entity;

import com.danrus.rpt.core.arm.ArmTransform;
import com.danrus.rpt.duck.CustomArmTransformHolder;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends HumanoidRenderState> {
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart head;

    @WrapMethod(method = "poseRightArm")
    private void rpt$wrapPoseRightArm(T renderState,
                                      //? <1.21.11
                                      //HumanoidModel.ArmPose pose,
                                      Operation<Void> original) {
        //? >= 1.21.11
        HumanoidModel.ArmPose pose = renderState.rightArmPose;

        ItemStackRenderState stackState =
                //? < 1.21.11 {
                /*renderState.rightHandItem;
                 *///? } else {
                renderState.rightHandItemState;
        //?}

        if (renderState instanceof AvatarRenderState && stackState instanceof CustomArmTransformHolder holder) {
            ArmTransform transform = holder.rpt$getRightArmTransform();

            HumanoidModel.ArmPose vanillaPose = transform.getVanillaIfPresent();
            if (vanillaPose != null) {
                if (!transform.swing()) rightArm.resetPose();
                //? >=1.21.11
                renderState.rightArmPose = vanillaPose;

                original.call(renderState
                        //? <1.21.11
                        //, vanillaPose
                );
                return;
            }

            if (!transform.isEmpty()) {
                transform.rotateModelPart(rightArm, head, true);
//                holder.rpt$setRightArmTransform(ArmTransform.EMPTY);
                return;
            }
        }
        original.call(renderState
                //? <1.21.11
                //, pose
        );
    }

    @WrapMethod(method = "poseLeftArm")
    private void rpt$wrapPoseLeftArm(T renderState,
                                     //? <1.21.11
                                     //HumanoidModel.ArmPose pose,
                                     Operation<Void> original) {
        //? >= 1.21.11
        HumanoidModel.ArmPose pose = renderState.leftArmPose;

        ItemStackRenderState stackState =
                //? < 1.21.11 {
                /*renderState.leftHandItem;
                *///? } else {
                renderState.leftHandItemState;
                //?}

        if (renderState instanceof AvatarRenderState && stackState instanceof CustomArmTransformHolder holder) {
            ArmTransform transform = holder.rpt$getLeftArmTransform();

            HumanoidModel.ArmPose vanillaPose = transform.getVanillaIfPresent();
            if (vanillaPose != null) {
                if (!transform.swing()) leftArm.resetPose();
                //? >= 1.21.11
                renderState.rightArmPose = vanillaPose;

                original.call(renderState
                        //? <1.21.11
                        //, vanillaPose
                );
                return;
            }

            if (!transform.isEmpty()) {
                transform.rotateModelPart(leftArm, head, false);
//                holder.rpt$setLeftArmTransform(ArmTransform.EMPTY);
                return;
            }
        }
        original.call(renderState
                //? <1.21.11
                //, pose
        );
    }
}
