package com.danrus.rpt.duck;

import com.danrus.rpt.core.anchor.AnchorType;
import com.mojang.blaze3d.vertex.PoseStack;

public interface CustomAnchorApplicableModel {
    void rpt$poseToAnchor(AnchorType anchor, PoseStack poseStack);
}
