package com.danrus.rpt.mixin.render.entity;

import com.danrus.rpt.core.arm.ArmTransform;
import com.danrus.rpt.core.arm.ArmTransformsHelper;
import com.danrus.rpt.duck.CustomArmTransformHolder;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Supplier;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends HumanoidRenderState> {
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart head;

    @WrapMethod(method = "poseRightArm")
    private void rpt$wrapPoseRightArm(T renderState,
                                      //? <1.21.11
                                      HumanoidModel.ArmPose pose,
                                      Operation<Void> original) {
        ItemStackRenderState stackState = ArmTransformsHelper.getRightItem(renderState);


        if (renderState instanceof PlayerRenderState playerRenderState && stackState instanceof CustomArmTransformHolder holder) {
            ArmTransform transform = holder.rpt$getRightArmTransform();

            HumanoidModel.ArmPose vanillaPose = transform.getVanillaIfPresent();
            if (vanillaPose != null) {
                if (!transform.swing()) rightArm.resetPose();
                //? >=1.21.11
                //renderState.rightArmPose = vanillaPose;

                original.call(renderState
                        //? <1.21.11
                        , vanillaPose
                );
                return;
            }

            if (!transform.isEmpty()) {
                transform.rotateModelPart(rightArm, head, true, playerRenderState);
                return;
            }
        }
        original.call(renderState
                //? <1.21.11
                , pose
        );
    }

    @WrapMethod(method = "poseLeftArm")
    private void rpt$wrapPoseLeftArm(T renderState,
                                     //? <1.21.11
                                     HumanoidModel.ArmPose pose,
                                     Operation<Void> original) {
        ItemStackRenderState stackState = ArmTransformsHelper.getLeftItem(renderState);

        if (renderState instanceof PlayerRenderState playerRenderState && stackState instanceof CustomArmTransformHolder holder) {
            ArmTransform transform = holder.rpt$getLeftArmTransform();

            HumanoidModel.ArmPose vanillaPose = transform.getVanillaIfPresent();
            if (vanillaPose != null) {
                if (!transform.swing()) leftArm.resetPose();
                //? >= 1.21.11
                //renderState.rightArmPose = vanillaPose;

                original.call(renderState
                        //? <1.21.11
                        , vanillaPose
                );
                return;
            }

            if (!transform.isEmpty()) {
                transform.rotateModelPart(leftArm, head, false, playerRenderState);
                return;
            }
        }
        original.call(renderState
                //? <1.21.11
                , pose
        );
    }

    @WrapMethod(method = "setupAttackAnimation")
    private void rpt$wrapAttack(HumanoidRenderState humanoidRenderState,
                                //? <=1.21.10
                                 float ageInTicks,
                                Operation<Void> original) {
        Runnable vanilla = () ->
                original.call(humanoidRenderState
                            //? <=1.21.10
                            , ageInTicks
                );

        if (humanoidRenderState instanceof PlayerRenderState playerRenderState) {
            ArmTransform transform;
            switch (playerRenderState.attackArm) {
                case LEFT -> transform = ((CustomArmTransformHolder) ArmTransformsHelper.getLeftItem(playerRenderState)).rpt$getLeftArmTransform();
                case RIGHT -> transform = ((CustomArmTransformHolder) ArmTransformsHelper.getRightItem(playerRenderState)).rpt$getRightArmTransform();
                default -> {
                    vanilla.run();
                    return;
                }
            }
            if (!transform.attack()) return;
            vanilla.run();
        } else {
            vanilla.run();
        }
    }
}
