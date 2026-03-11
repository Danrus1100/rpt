package com.danrus.rpt.mixin.render;

import com.danrus.rpt.Rpt;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public class GuiMixin {

    @WrapOperation(
            method = "renderSpyglassOverlay",
            at  = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIFFIIII)V", ordinal = 0)
    )
    private static void rpt$wrapSpyglassOverlay(GuiGraphics instance, RenderPipeline pipeline, ResourceLocation atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, Operation<Void> original) {
        original.call(instance, pipeline, Rpt.getTextureSwappersManager().swap(atlas, Minecraft.getInstance().player.getUseItem(), Minecraft.getInstance().player), x, y, u, v, width, height, textureHeight, textureHeight);
    }

    @WrapOperation(
            method = "renderCameraOverlays",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;")
    )
    private static ItemStack rpt$saveItemStack(LocalPlayer instance, EquipmentSlot equipmentSlot, Operation<ItemStack> original, @Share("itemStack") LocalRef<ItemStack> stackLocalRef) {
        ItemStack stack = original.call(instance, equipmentSlot);
        stackLocalRef.set(stack);
        return stack;
    }


    @WrapOperation(
            method = "renderCameraOverlays",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderTextureOverlay(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;F)V")
    )
    private static void rpt$wrapPumpkinOverlay(Gui instance, GuiGraphics guiGraphics, ResourceLocation shaderLocation, float alpha, Operation<Void> original, @Share("itemStack") LocalRef<ItemStack> stackLocalRef) {
        original.call(instance, guiGraphics, Rpt.getTextureSwappersManager().swap(shaderLocation, stackLocalRef.get(), Minecraft.getInstance().player), alpha);
    }
}
