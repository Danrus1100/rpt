package com.danrus.rpt.mixin.render.fpa;

import com.danrus.rpt.Rpt;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Shadow
    public abstract void renderItem(LivingEntity par1, ItemStack par2, ItemDisplayContext par3, PoseStack par4, MultiBufferSource par5, int par6);

    @WrapMethod(
            method = "renderArmWithItem"
    )
    private void rpt$renderArmWithItem(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, Operation<Void> original) {
        Runnable orig = () -> original.call(player, partialTicks, pitch, hand, swingProgress, stack, equippedProgress, poseStack, buffer, combinedLight);
        var transform = Rpt.getFpaManager().getTransforms(stack);
        if (transform.isDefault()) {
            orig.run();
            return;
        }
        boolean isHandMain = hand == InteractionHand.MAIN_HAND;
        HumanoidArm humanoidArm = isHandMain ? player.getMainArm() : player.getMainArm().getOpposite();
        boolean isHandRight = humanoidArm == HumanoidArm.RIGHT;
        poseStack.pushPose();
        if (!transform.applyToPose(player, hand, isHandRight, equippedProgress, poseStack)) {
            orig.run();
            poseStack.popPose();
            return;
        }
        this.renderItem(player, stack, isHandRight ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, poseStack, buffer, combinedLight);
        poseStack.popPose();
    }
}
