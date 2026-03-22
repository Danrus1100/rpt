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
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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

import java.util.function.Function;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends HumanoidRenderState> {
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart head;

    @Unique
    private CustomTransformsDispatcher dispatcher = null;

    @Inject(
            method = "<init>(Lnet/minecraft/client/model/geom/ModelPart;Ljava/util/function/Function;)V",
            at = @At("RETURN")
    )
    private void rpt$injectInit(ModelPart root, Function renderType, CallbackInfo ci) {
        dispatcher = new CustomTransformsDispatcher(rightArm, leftArm, head);
    }

    @Definition(id = "mainArm", field = "Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;mainArm:Lnet/minecraft/world/entity/HumanoidArm;")
    @Definition(id = "renderState", local = @Local(argsOnly = true, type = HumanoidRenderState.class))
    @Definition(id = "RIGHT", field = "Lnet/minecraft/world/entity/HumanoidArm;RIGHT:Lnet/minecraft/world/entity/HumanoidArm;")
    @Expression("renderState.mainArm == RIGHT")
    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
            at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private void rpt$injectHui(T renderState, CallbackInfo ci) {
        if (renderState instanceof PlayerRenderState && dispatcher != null) {
            dispatcher.dispatch(renderState);
        }
    }

    @WrapMethod(method = "poseRightArm")
    private void rpt$wrapPoseRightArm(T renderState,
                                      //? <1.21.11
                                      HumanoidModel.ArmPose pose,
                                      Operation<Void> original) {
        if (renderState instanceof CustomTransformsDispatchedState dispatchedState && dispatcher != null) {
            if (dispatchedState.rpt$isAlreadyTransformed(HumanoidArm.RIGHT)) {
                return;
            }

            HumanoidModel.ArmPose armPose = dispatcher.getVanilla(renderState, HumanoidArm.RIGHT);

            if (armPose != null) {
                //? <1.21.11 {
                original.call(renderState
                        , armPose
                );
                //?} else {
                /*renderState.rightArmPose = armPose;
                original.call(renderState);
                *///?}
                return;
            } else {
                original.call(renderState
                        //? <1.21.11
                        , pose
                );
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
        if (renderState instanceof CustomTransformsDispatchedState dispatchedState && dispatcher != null) {
            if (dispatchedState.rpt$isAlreadyTransformed(HumanoidArm.LEFT)) {
                return;
            }

            HumanoidModel.ArmPose armPose = dispatcher.getVanilla(renderState, HumanoidArm.LEFT);

            if (armPose != null) {
                //? <1.21.11 {
                original.call(renderState
                        , armPose
                );
                //?} else {
                /*renderState.leftArmPose = armPose;
                original.call(renderState);
                *///?}
                return;
            } else {
                original.call(renderState
                        //? <1.21.11
                        , pose
                );
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

    @WrapOperation(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;bobModelPart(Lnet/minecraft/client/model/geom/ModelPart;FF)V")
    )
    private void rpt$bobModel(ModelPart modelPart, float ageInTicks, float multiplier, Operation<Void> original, @Local(argsOnly = true) T renderState) {
        HumanoidArm arm = modelPart == rightArm ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        if (!dispatcher.shouldCancelBob(renderState, arm)) {
            original.call(modelPart, ageInTicks, multiplier);
        }
    }
}
