package com.danrus.rpt.mixin.render.entity;

import com.danrus.rpt.duck.LivingEntityHolder;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>> {
    @WrapMethod(
            method = "getArmorModel"
    )
    private A rpt$wrapArmorModelGetter(S renderState, EquipmentSlot slot, Operation<A> original) {
        A toReturn = original.call(renderState, slot);
        ((LivingEntityHolder) toReturn).rpt$setEntity(((LivingEntityHolder) renderState).rpt$getEntity());
        return toReturn;
    }
}
