package com.danrus.rpt.mixin.render.fpa;

import com.danrus.rpt.Rpt;
import com.danrus.rpt.core.fpa.CustomFirstPersonTransforms;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Unique
    private static final ThreadLocal<Boolean> rpt$skipVanillaTransform = ThreadLocal.withInitial(() -> false);

    @WrapMethod(method = "submitArmWithItem")
    private void rpt$renderArmWithItem(
            PlayerRenderState playerState,
            FirstPersonHandsAndItemsRenderState renderState,
            float partialTicks,
            float pitch,
            InteractionHand hand,
            float swingProgress,
            ItemStack stack,
            float equippedProgress,
            PoseStack poseStack,
            SubmitNodeCollector buffer,
            int combinedLight,
            Operation<Void> original
    ) {
        CustomFirstPersonTransforms transform = Rpt.getFpaManager().getTransforms(stack);
        AbstractClientPlayer player = Minecraft.getInstance().player;

        if (transform.isDefault() || player == null) {
            original.call(playerState, renderState, partialTicks, pitch, hand, swingProgress, stack, equippedProgress, poseStack, buffer, combinedLight);
            return;
        }

        boolean isHandMain = hand == InteractionHand.MAIN_HAND;
        HumanoidArm humanoidArm = isHandMain ? player.getMainArm() : player.getMainArm().getOpposite();
        boolean isHandRight = humanoidArm == HumanoidArm.RIGHT;

        poseStack.pushPose();
        transform.applyToPose(player, hand, !isHandRight, equippedProgress, poseStack);

        // Vanilla pose transforms must not be applied twice on top of the custom transform.
        rpt$skipVanillaTransform.set(!transform.vanilla());
        try {
            original.call(playerState, renderState, partialTicks, pitch, hand, swingProgress, stack, equippedProgress, poseStack, buffer, combinedLight);
        } finally {
            rpt$skipVanillaTransform.set(false);
        }
        poseStack.popPose();
    }

    @WrapOperation(
            method = "submitArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/FirstPersonHandsAndItemsRenderer;applyItemArmTransform(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V"
            )
    )
    private void rpt$applyItemArmTransform(
            FirstPersonHandsAndItemsRenderer instance,
            PoseStack poseStack,
            HumanoidArm arm,
            float equippedProgress,
            Operation<Void> original
    ) {
        if (!rpt$skipVanillaTransform.get()) {
            original.call(instance, poseStack, arm, equippedProgress);
        }
    }

    @WrapOperation(
            method = "submitArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/FirstPersonHandsAndItemsRenderer;swingArm(FLcom/mojang/blaze3d/vertex/PoseStack;ILnet/minecraft/world/entity/HumanoidArm;)V"
            )
    )
    private void rpt$swingArm(
            FirstPersonHandsAndItemsRenderer instance,
            float swingProgress,
            PoseStack poseStack,
            int direction,
            HumanoidArm arm,
            Operation<Void> original
    ) {
        if (!rpt$skipVanillaTransform.get()) {
            original.call(instance, swingProgress, poseStack, direction, arm);
        }
    }
}
