package com.danrus.rpt.mixin.input;

import com.danrus.rpt.Rpt;
import com.danrus.rpt.core.bbmodel.fsm.FsmTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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
public class MinecraftInteractionMixin {

    @Unique
    private int rpt$lastAttckTick = 0;

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void rpt$prefomAttackTick(CallbackInfo ci) {
        rpt$lastAttckTick++;
    }

    @Inject(method = "startAttack", at = @At("HEAD"))
    private void rpt$startAttack(CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        Rpt.getBbmodelsManager().triggerForHand(FsmTriggers.ATTACK, player, stack, toDisplayContext(player, InteractionHand.MAIN_HAND));
//        Rpt.getBbmodelsManager().triggerForHand(rpt$lastAttckTick <= 10 ? FsmTriggers.ATTACK2 : FsmTriggers.ATTACK1, player, stack, toDisplayContext(player, InteractionHand.MAIN_HAND));
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
        ItemStack stack = player.getItemInHand(hand);
        Rpt.getBbmodelsManager().triggerForHand(FsmTriggers.USE, player, stack, toDisplayContext(player, hand));
    }

    @Unique
    private static ItemDisplayContext toDisplayContext(LocalPlayer player, InteractionHand hand) {
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        return arm == HumanoidArm.RIGHT
                ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
    }
}
