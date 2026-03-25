package com.danrus.rpt.mixin.render.bb;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.rpt.core.bbmodel.fsm.FsmInstance;
import com.danrus.rpt.core.bbmodel.nodes.BbModelsSubmitsCollector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(net.minecraft.client.renderer.SubmitNodeCollection.class)
public class SubmitNodeCollectionMixin implements BbModelsSubmitsCollector {

    @Unique
    private final List<BbModelSubmit> bbModelSubmits = new ArrayList<>();

    @Override
    public void rpt$submitBbModel(PoseStack pose, BbModelDocument model, @Nullable FsmInstance dynamicState, @Nullable Identifier playerSkin, int packedLight, int packedOverlay) {
        bbModelSubmits.add(new BbModelSubmit(pose.last().copy(), model, dynamicState, playerSkin, packedLight, packedOverlay));
    }

    @Override
    public List<BbModelSubmit> rpt$getBbModelsSubmits() {
        return bbModelSubmits;
    }

    @Inject(
            method = "clear",
            at = @At("HEAD")
    )
    private void rpt$clearSubmits(CallbackInfo ci) {
        bbModelSubmits.clear();
    }
}
