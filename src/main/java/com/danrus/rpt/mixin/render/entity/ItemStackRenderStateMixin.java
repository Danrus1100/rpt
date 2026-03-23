package com.danrus.rpt.mixin.render.entity;

import com.danrus.rpt.core.anchor.AnchorType;
import com.danrus.rpt.core.arm.ArmTransform;
import com.danrus.rpt.duck.RptItemRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({
        ItemStackRenderState.class
})
public class ItemStackRenderStateMixin implements RptItemRenderState {

    @Unique
    private ArmTransform rpt$leftArmTransform = ArmTransform.EMPTY;

    @Unique
    private ArmTransform rpt$rightArmTransform = ArmTransform.EMPTY;

    @Unique
    private AnchorType rpt$anchorType = null;

    @Override
    public void rpt$setLeftArmTransform(ArmTransform transform) {
        rpt$leftArmTransform = transform;
    }

    @Override
    public void rpt$setRightArmTransform(ArmTransform transform) {
        rpt$rightArmTransform = transform;
    }

    @Override
    public ArmTransform rpt$getLeftArmTransform() {
        return rpt$leftArmTransform;
    }

    @Override
    public ArmTransform rpt$getRightArmTransform() {
        return rpt$rightArmTransform;
    }

    @Override
    public @Nullable AnchorType rpt$getAnchorType() {
        return rpt$anchorType;
    }

    @Override
    public void rpt$setAnchorType(@Nullable AnchorType type) {
        rpt$anchorType = type;
    }

    @Inject(
            method = "clear",
            at = @At("HEAD")
    )
    private void rpt$onClear(CallbackInfo ci) {
        rpt$setRightArmTransform(ArmTransform.EMPTY);
        rpt$setLeftArmTransform(ArmTransform.EMPTY);
        rpt$setAnchorType(null);
    }
}
