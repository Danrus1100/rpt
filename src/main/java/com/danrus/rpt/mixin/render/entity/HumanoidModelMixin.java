package com.danrus.rpt.mixin.render.entity;

import com.danrus.rpt.core.arm.ArmTransform;
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
        //? >= 1.21.11
        //HumanoidModel.ArmPose pose = renderState.rightArmPose;

        ItemStackRenderState stackState = rpt$getRightItem(renderState);


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
        //? >= 1.21.11
        //HumanoidModel.ArmPose pose = renderState.leftArmPose;

        ItemStackRenderState stackState = rpt$getLeftItem(renderState);

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
                case LEFT -> transform = ((CustomArmTransformHolder)rpt$getLeftItem(playerRenderState)).rpt$getLeftArmTransform();
                case RIGHT -> transform = ((CustomArmTransformHolder)rpt$getRightItem(playerRenderState)).rpt$getRightArmTransform();
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

    @Unique
    private static <T extends HumanoidRenderState> ItemStackRenderState rpt$getRightItem(T renderState) {
        return
        //? < 1.21.11 {
        renderState.rightHandItem;
         //? } else {
        /*renderState.rightHandItemState;
        *///?}
    }

    @Unique
    private static <T extends HumanoidRenderState> ItemStackRenderState rpt$getLeftItem(T renderState) {
        return
        //? < 1.21.11 {
        renderState.leftHandItem;
         //? } else {
        /*renderState.leftHandItemState;
        *///?}
    }
}
