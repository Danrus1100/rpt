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
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

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

        ItemStackRenderState stackState = rpt$getRightStackState(renderState);

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

        ItemStackRenderState stackState = rpt$getLeftStackState(renderState);

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

    @WrapMethod(
            method = "setupAttackAnimation"
    )
    private void rpt$wrapAttack(T renderState,
                                // ? <=1.21.10
                                float ageInTicks,
                                Operation<Void> original) {
        if (renderState instanceof PlayerRenderState playerRenderState) {
            ArmTransform transform;
            switch (playerRenderState.attackArm) {
                case LEFT ->  transform = ((CustomArmTransformHolder) rpt$getLeftStackState(renderState)).rpt$getLeftArmTransform();
                case RIGHT -> transform = ((CustomArmTransformHolder) rpt$getRightStackState(renderState)).rpt$getRightArmTransform();
                default -> { return; }
            }
            if (transform.attack()) {
                original.call(renderState
                        //? <=1.21.10
                        , ageInTicks
                );
            }
        } else {
            original.call(renderState
                    //? <=1.21.10
                    , ageInTicks
            );
        }
    }

    @Unique
    private ItemStackRenderState rpt$getLeftStackState(T renderState) {
        return
        //? < 1.21.11 {
        renderState.leftHandItem;
        //? } else {
        /*renderState.leftHandItemState;
         *///?}
    }

    @Unique
    private ItemStackRenderState rpt$getRightStackState(T renderState) {
        return
        //? < 1.21.11 {
        renderState.rightHandItem;
        //? } else {
        /*renderState.rightHandItemState;
         *///?}
    }
}
