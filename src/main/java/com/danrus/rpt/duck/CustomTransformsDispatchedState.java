package com.danrus.rpt.duck;

import net.minecraft.world.entity.HumanoidArm;

public interface CustomTransformsDispatchedState {
    void rpt$markAsAlreadyTransformed(HumanoidArm arm);
    void rpt$reset();
    boolean rpt$isAlreadyTransformed(HumanoidArm arm);
}
