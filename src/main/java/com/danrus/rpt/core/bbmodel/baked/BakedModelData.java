package com.danrus.rpt.core.bbmodel.baked;

import com.danrus.rpt.core.bbmodel.RptBbModelUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public record BakedModelData(
        List<RptBakedMesh> staticMeshes,
        Map<String, RptBbModelUtils.TextureRenderData> textureRenderDataCache
) {
    public BakedModelData(List<RptBakedMesh> staticMeshes) {
        this(staticMeshes, new ConcurrentHashMap<>());
    }
}
