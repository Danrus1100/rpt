package com.danrus.rpt.duck;

import com.danrus.rpt.core.arm.ArmTransform;

public interface CustomArmTransformHolder {
    void rpt$setLeftArmTransform(ArmTransform transform);
    void rpt$setRightArmTransform(ArmTransform transform);

    ArmTransform rpt$getLeftArmTransform();
    ArmTransform rpt$getRightArmTransform();
}
