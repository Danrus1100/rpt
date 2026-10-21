package com.danrus.rpt.core.arm;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Unique;

public class ArmTransformsHelper {
    @Unique
    public static <T extends HumanoidRenderState> ItemStackRenderState getRightItem(T renderState) {
        return
        renderState.rightHandItemState;
    }

    @Unique
    public static <T extends HumanoidRenderState> ItemStackRenderState getLeftItem(T renderState) {
        return
        renderState.leftHandItemState;
    }
}
