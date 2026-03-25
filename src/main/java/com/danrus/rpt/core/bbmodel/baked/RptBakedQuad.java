package com.danrus.rpt.core.bbmodel.baked;

public record RptBakedQuad(
        float x0, float y0, float z0, float u0, float v0,
        float x1, float y1, float z1, float u1, float v1,
        float x2, float y2, float z2, float u2, float v2,
        float x3, float y3, float z3, float u3, float v3,
        float nx, float ny, float nz,
        float centerX, float centerY, float centerZ,
        String textureReference,
        boolean hasTextureReference
) {
}
