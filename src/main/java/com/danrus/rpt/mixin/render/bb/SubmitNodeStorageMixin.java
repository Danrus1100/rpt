package com.danrus.rpt.mixin.render.bb;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.rpt.core.bbmodel.fsm.FsmInstance;
import com.danrus.rpt.core.bbmodel.nodes.BbModelsSubmitsCollector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(net.minecraft.client.renderer.SubmitNodeStorage.class)
public abstract class SubmitNodeStorageMixin implements BbModelsSubmitsCollector {
    @Shadow
    public abstract SubmitNodeCollection order(int i);

    @Override
    public void rpt$submitBbModel(PoseStack pose, BbModelDocument model, @Nullable FsmInstance dynamicState, @Nullable Identifier playerSkin, int packedLight, int packedOverlay) {
        ((BbModelsSubmitsCollector) this.order(0)).rpt$submitBbModel(pose, model, dynamicState, playerSkin, packedLight, packedOverlay);
    }

    @Override
    public List<BbModelSubmit> rpt$getBbModelsSubmits() {
        return List.of(); //NO-OP here
    }
}
