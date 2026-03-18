package com.danrus.rpt.mixin.render.entity;

import com.danrus.rpt.duck.LivingEntityHolder;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidMobRenderer.class)
public class HumanoidMobRendererMixin {

    @Inject(
            method = "extractHumanoidRenderState",
            at = @At("HEAD")
    )
    private static void rpt$extractEntity(LivingEntity entity, HumanoidRenderState reusedState, float partialTick, ItemModelResolver itemModelResolver, CallbackInfo ci) {
       ((LivingEntityHolder) reusedState).rpt$setEntity(entity);
    }
}
