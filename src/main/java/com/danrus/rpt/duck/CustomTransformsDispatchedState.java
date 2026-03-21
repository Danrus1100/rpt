package com.danrus.rpt.duck;

import net.minecraft.world.entity.HumanoidArm;

public interface CustomTransformsDispatchedState {
    void rpt$markAsAlreadyTransformed(HumanoidArm arm);
    boolean rpt$isAlreadyTransformed(HumanoidArm arm);
}
