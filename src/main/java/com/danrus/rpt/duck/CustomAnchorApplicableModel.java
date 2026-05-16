package com.danrus.rpt.duck;

import com.danrus.rpt.core.anchor.AnchorType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;

public interface CustomAnchorApplicableModel {
    void rpt$poseToAnchor(AnchorType anchor, PoseStack poseStack);

    public static void poseToAnchor(HumanoidModel<?> model, AnchorType anchor, PoseStack poseStack) {
        CustomAnchorApplicableModel.class.cast(model).rpt$poseToAnchor(anchor, poseStack);
    }
}
