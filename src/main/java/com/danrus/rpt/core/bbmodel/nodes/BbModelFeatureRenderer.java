package com.danrus.rpt.core.bbmodel.nodes;

import com.danrus.bb4j.api.utils.RenderUtils;
import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.rpt.core.bbmodel.BbModelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class BbModelFeatureRenderer {
    private final Map<BbModelDocument, List<RenderUtils.RenderableMesh>> STATIC_MESH_CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    public void renderAll(BbModelsSubmitsCollector nodeCollection, MultiBufferSource.BufferSource bufferSource) {
        nodeCollection.rpt$getBbModelsSubmits().forEach(submit -> {
            if (isStaticSubmit(submit)) {
                BbModelRenderer.get().renderStaticToBuffer(
                        submit.model(),
                        bufferSource,
                        submit.pose(),
                        submit.packedLight(),
                        submit.packedOverlay(),
                        submit.playerSkin()
                );
            } else {
                BbModelRenderer.get().renderDynamicToBuffer(
                        submit.model(),
                        bufferSource,
                        submit.pose(),
                        submit.packedLight(),
                        submit.packedOverlay(),
                        submit.dynamicState().getBlendStates(),
                        submit.playerSkin()
                );
            }
        });
    }

    private static boolean isStaticSubmit(BbModelsSubmitsCollector.BbModelSubmit submit) {
        return submit.dynamicState() == null || submit.dynamicState().getBlendStates() == null || submit.dynamicState().getBlendStates().isEmpty();
    }
}
