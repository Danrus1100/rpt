package com.danrus.rpt.mixin.render.entity;

import com.danrus.rpt.duck.CustomTransformsDispatchedState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(HumanoidRenderState.class)
public class CustomTransformsDispatchedStateMixin implements CustomTransformsDispatchedState {

    @Unique
    private boolean rpt$isRightAlreadyTransformed = false;
    @Unique
    private boolean rpt$isLeftAlreadyTransformed = false;

    @Override
    public void rpt$markAsAlreadyTransformed(HumanoidArm arm) {
        switch (arm) {
            case LEFT -> rpt$isLeftAlreadyTransformed = true;
            case RIGHT -> rpt$isRightAlreadyTransformed = true;
        }
    }

    @Override
    public boolean rpt$isAlreadyTransformed(HumanoidArm arm) {
        return switch (arm) {
            case LEFT -> rpt$isLeftAlreadyTransformed;
            case RIGHT -> rpt$isRightAlreadyTransformed;
        };
    }
}
