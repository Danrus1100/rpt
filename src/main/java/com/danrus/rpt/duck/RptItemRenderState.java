package com.danrus.rpt.duck;

import com.danrus.rpt.core.anchor.AnchorType;
import com.danrus.rpt.core.arm.ArmTransform;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.Nullable;

public interface RptItemRenderState {
    void rpt$setLeftArmTransform(ArmTransform transform);
    void rpt$setRightArmTransform(ArmTransform transform);

    ArmTransform rpt$getLeftArmTransform();
    ArmTransform rpt$getRightArmTransform();

    @Nullable AnchorType rpt$getAnchorType();
    void rpt$setAnchorType(@Nullable AnchorType type);

    default ArmTransform getForArm(HumanoidArm arm) {
        return switch (arm) {
            case LEFT -> rpt$getLeftArmTransform();
            case RIGHT -> rpt$getRightArmTransform();
        };
    }

    default boolean isBothHandsAvailable(HumanoidArm mainArm) {
        switch (mainArm) {
            case RIGHT -> {
                if (rpt$getLeftArmTransform().isEmpty()) {
                    return true;
                }
                return rpt$getRightArmTransform().isEmpty();
            }
            case LEFT -> {
                if (rpt$getRightArmTransform().isEmpty()) {
                    return true;
                }
                return rpt$getLeftArmTransform().isEmpty();
            }
        }
        return false;
    }
}
