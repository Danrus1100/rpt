package com.danrus.rpt.mixin;

import com.danrus.rpt.Rpt;
import com.danrus.rpt.RptHooks;
import com.danrus.rpt.core.bbmodel.fsm.FsmTriggers;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @WrapMethod(method = "tick")
    private void rpt$hookTick(Operation<Void> original) {
        RptHooks.preTick();
        original.call();
        RptHooks.postTick();
    }

    @WrapOperation(
            method = "runTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V")
    )
    private void rpt$hookRender(GameRenderer instance, DeltaTracker deltaTracker, boolean renderLevel, Operation<Void> original) {
        RptHooks.preRender(instance, deltaTracker);
        original.call(instance, deltaTracker, renderLevel);
        RptHooks.postRender(instance, deltaTracker);
    }

    @Inject(method = "startAttack", at = @At("HEAD"))
    private void rpt$startAttack(CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        Rpt.getFsmManager().trigger(FsmTriggers.ATTACK, toDisplayContext(player, InteractionHand.MAIN_HAND), player);
    }

    @Inject(method = "startUseItem", at = @At("HEAD"))
    private void rpt$startUseItem(CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        triggerUseForHand(player, InteractionHand.MAIN_HAND);
        triggerUseForHand(player, InteractionHand.OFF_HAND);
    }

    @Unique
    private static void triggerUseForHand(LocalPlayer player, InteractionHand hand) {
        Rpt.getFsmManager().trigger(FsmTriggers.USE, toDisplayContext(player, hand), player);
    }

    @Unique
    private static ItemDisplayContext toDisplayContext(LocalPlayer player, InteractionHand hand) {
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        return arm == HumanoidArm.RIGHT
                ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
    }
}
