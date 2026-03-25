package com.danrus.rpt.core.bbmodel.baked;

import java.util.List;

public record RptBakedMesh(
        float posX, float posY, float posZ,
        float originX, float originY, float originZ,
        float rotX, float rotY, float rotZ,
        float scaleX, float scaleY, float scaleZ,
        boolean hasRotation, boolean hasScale,
        List<RptBakedQuad> quads,
        List<String> hierarchy,
        List<BakedTransformStep> transformSteps
) {
    public record BakedTransformStep(
            String uuid,
            float originX, float originY, float originZ,
            float posX, float posY, float posZ,
            float rotX, float rotY, float rotZ,
            float scaleX, float scaleY, float scaleZ
    ) {}
}
