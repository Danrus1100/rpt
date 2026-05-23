package com.danrus.rpt.mixin.render.entity;

import com.danrus.rpt.Rpt;
import com.danrus.rpt.duck.LivingEntityHolder;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(WingsLayer.class)
public class WingsLayerMixin {
    @WrapMethod(
            method = "getPlayerElytraTexture"
    )
    private static ResourceLocation rpt$swapElytra(HumanoidRenderState state, Operation<ResourceLocation> original) {
        ResourceLocation location = original.call(state);
        AtomicReference<ResourceLocation> swap = new AtomicReference<>(location);
        Rpt.getTextureSwappersManager().swap(location, state.chestEquipment, ((state instanceof LivingEntityHolder holder) ? holder.rpt$getEntity() : null), swap::set);
        return swap.get();
    }
}
