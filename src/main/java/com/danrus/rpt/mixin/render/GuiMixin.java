package com.danrus.rpt.mixin.render;

import com.danrus.rpt.Rpt;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(
        net.minecraft.client.gui.Hud.class
)
public class GuiMixin {

    @WrapOperation(
            method = "extractSpyglassOverlay",
            at  = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V", ordinal = 0)
    )
    private static void rpt$wrapSpyglassOverlay(GuiGraphicsExtractor instance, RenderPipeline pipeline, Identifier atlas, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight, Operation<Void> original) {
        Rpt.getTextureSwappersManager().swap(atlas, Minecraft.getInstance().player.getUseItem(), Minecraft.getInstance().player, location -> {
            original.call(instance, pipeline, location, x, y, u, v, width, height, textureWidth, textureHeight);
        });
    }

    @WrapOperation(
            method = "extractCameraOverlays",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;")
    )
    private static ItemStack rpt$saveItemStack(LocalPlayer instance, EquipmentSlot equipmentSlot, Operation<ItemStack> original, @Share("itemStack") LocalRef<ItemStack> stackLocalRef) {
        ItemStack stack = original.call(instance, equipmentSlot);
        stackLocalRef.set(stack);
        return stack;
    }


    @WrapOperation(
            method = "extractCameraOverlays",
            at = @At(value = "INVOKE", target =
                    "Lnet/minecraft/client/gui/Hud;extractTextureOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;F)V"
            )
    )
    private static void rpt$wrapPumpkinOverlay(
            net.minecraft.client.gui.Hud
            instance, GuiGraphicsExtractor guiGraphics, Identifier shaderLocation, float alpha, Operation<Void> original, @Share("itemStack") LocalRef<ItemStack> stackLocalRef) {
        ItemStack stack = stackLocalRef.get();
        if (stack == null) {
            original.call(instance, guiGraphics, shaderLocation, alpha);
            return;
        }

        Rpt.getTextureSwappersManager().swap(shaderLocation, stack, Minecraft.getInstance().player, location -> {
            original.call(instance, guiGraphics, location, alpha);
        });
        // The stack belongs to one equipment slot; later overlays (e.g. powder snow)
        // must not be evaluated against it.
        stackLocalRef.set(null);
    }
}
