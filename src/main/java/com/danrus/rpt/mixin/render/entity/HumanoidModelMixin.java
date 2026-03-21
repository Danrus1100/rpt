package com.danrus.rpt.mixin.render.entity;

import com.danrus.rpt.core.arm.ArmTransform;
import com.danrus.rpt.core.arm.ArmTransformsHelper;
import com.danrus.rpt.core.arm.CustomTransformsDispatcher;
import com.danrus.rpt.duck.CustomArmTransformHolder;
import com.danrus.rpt.duck.CustomTransformsDispatchedState;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends HumanoidRenderState> {
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart head;

    @Unique
    private final CustomTransformsDispatcher dispatcher = new CustomTransformsDispatcher(rightArm, leftArm, head);

    @Definition(id = "mainArm", field = "Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;mainArm:Lnet/minecraft/world/entity/HumanoidArm;")
    @Definition(id = "renderState", local = @Local(argsOnly = true, type = HumanoidRenderState.class))
    @Definition(id = "RIGHT", field = "Lnet/minecraft/world/entity/HumanoidArm;RIGHT:Lnet/minecraft/world/entity/HumanoidArm;")
    @Expression("renderState.mainArm == RIGHT")
    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
            at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private void rpt$injectHui(T renderState, CallbackInfo ci) {
        dispatcher.dispatch(renderState);
    }

    @WrapMethod(method = "poseRightArm")
    private void rpt$wrapPoseRightArm(T renderState,
                                      //? <1.21.11
                                      HumanoidModel.ArmPose pose,
                                      Operation<Void> original) {
        if (renderState instanceof CustomTransformsDispatchedState dispatchedState) {
            if (dispatchedState.rpt$isAlreadyTransformed(HumanoidArm.RIGHT)) {
                return;
            }

            ItemStackRenderState stackState = ArmTransformsHelper.getRightItem(renderState);
            if (stackState instanceof CustomArmTransformHolder holder) {
                ArmTransform transform = holder.rpt$getRightArmTransform();

                if (!transform.isEmpty() && transform.getVanillaOrNull() != null) {
                    //? <1.21.11 {
                    original.call(renderState
                            , transform.getVanillaOrNull()
                    );
                    //?} else {
                    /*renderState.leftArmPose = transform.getVanillaOrNull();
                    original.call(renderState);
                    *///}
                    return;
                }
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
        if (renderState instanceof CustomTransformsDispatchedState dispatchedState) {
            if (dispatchedState.rpt$isAlreadyTransformed(HumanoidArm.LEFT)) {
                return;
            }

            ItemStackRenderState stackState = ArmTransformsHelper.getLeftItem(renderState);
            if (stackState instanceof CustomArmTransformHolder holder) {
                ArmTransform transform = holder.rpt$getLeftArmTransform();

                if (!transform.isEmpty() && transform.getVanillaOrNull() != null) {
                    //? <1.21.11 {
                    original.call(renderState
                            , transform.getVanillaOrNull()
                    );
                    //?} else {
                    /*renderState.rightArmPose = transform.getVanillaOrNull();
                    original.call(renderState);
                    *///}
                    return;
                }
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

        if (dispatcher.shouldCancelAttack(humanoidRenderState)) {
            return;
        }

        vanilla.run();
    }
}
