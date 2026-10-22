package com.danrus.rpt.mixin.render.entity;

import com.danrus.rpt.core.arm.CustomTransformsDispatcher;
import net.minecraft.client.model.object.armorstand.ArmorStandModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStandModel.class)
public abstract class ArmorStandModelMixin extends HumanoidModel {

    @Unique
    private CustomTransformsDispatcher dispatcher = null;

    public ArmorStandModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void rpt$injectInit(ModelPart root, CallbackInfo ci) {
        dispatcher = new CustomTransformsDispatcher(rightArm, leftArm, head);
    }


    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;)V",
            at = @At("TAIL")
    )
    private void rpt$animateArmorStandArms(ArmorStandRenderState renderState, CallbackInfo ci) {
        if (dispatcher != null) {
            dispatcher.dispatch(renderState);
        }
    }
}
