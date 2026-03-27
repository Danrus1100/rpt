package com.danrus.rpt.mixin.render.bb;

import com.danrus.rpt.Rpt;
import com.danrus.rpt.core.bbmodel.fsm.FsmTriggers;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Shadow private ItemStack mainHandItem;
    @Shadow private ItemStack offHandItem;

    @Inject(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;offHandItem:Lnet/minecraft/world/item/ItemStack;",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.BEFORE
            )
    )
    private void rpt$afterAnyOffhandSet(CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        var newStack = player.getOffhandItem();

        if (Objects.equals(newStack.get(DataComponents.ITEM_MODEL), offHandItem.get(DataComponents.ITEM_MODEL))) return;

        ItemDisplayContext context = player.getMainArm().getOpposite() == HumanoidArm.RIGHT
                ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        Rpt.getFsmManager().trigger(FsmTriggers.DRAW, context, player);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;mainHandItem:Lnet/minecraft/world/item/ItemStack;",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.BEFORE
            )
    )
    private void rpt$afterMainHandSet(CallbackInfo ci) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        var newStack = player.getMainHandItem();

        if (Objects.equals(newStack.get(DataComponents.ITEM_MODEL), mainHandItem.get(DataComponents.ITEM_MODEL))) return;

        ItemDisplayContext context = player.getMainArm() == HumanoidArm.RIGHT
                ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        Rpt.getFsmManager().trigger(FsmTriggers.DRAW, context, player);
    }
}
