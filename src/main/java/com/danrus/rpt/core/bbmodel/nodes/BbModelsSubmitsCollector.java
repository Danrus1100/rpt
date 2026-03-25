package com.danrus.rpt.core.bbmodel.nodes;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.rpt.core.bbmodel.fsm.FsmInstance;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface BbModelsSubmitsCollector {
    void rpt$submitBbModel(PoseStack pose, BbModelDocument model, @Nullable FsmInstance dynamicState, @Nullable Identifier playerSkin, int packedLight, int packedOverlay);
    List<BbModelSubmit> rpt$getBbModelsSubmits();

    public static BbModelsSubmitsCollector from(SubmitNodeCollector collector) {
        return (BbModelsSubmitsCollector) collector;
    }

    record BbModelSubmit(PoseStack.Pose pose, BbModelDocument model, @Nullable FsmInstance dynamicState, @Nullable Identifier playerSkin, int packedLight, int packedOverlay){}
}
