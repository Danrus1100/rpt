package com.danrus.rpt.core.bbmodel;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.animation.AnimationBlendState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Set;

public interface BbModelRenderer {
    void renderToBuffer(BbModelDocument model, MultiBufferSource bufferSource, PoseStack poseStack, int packedLight, int packedOverlay);
    void renderToBuffer(BbModelDocument model, MultiBufferSource bufferSource, PoseStack poseStack, int packedLight, int packedOverlay, @Nullable LivingEntity holder);
    void renderToBuffer(BbModelDocument model, MultiBufferSource bufferSource, PoseStack poseStack, int packedLight, int packedOverlay, List<AnimationBlendState> activeAnimations, @Nullable LivingEntity holder);
    void renderToBuffer(BbModelDocument model, MultiBufferSource bufferSource, PoseStack poseStack, int packedLight, int packedOverlay, String animation, double animationTime, @Nullable LivingEntity holder);

    void getExtentsForGui(BbModelDocument model, PoseStack poseStack, Set<Vector3f> output);
    void getExtentsForGui(BbModelDocument model, PoseStack poseStack, Set<Vector3f> output, String animation, double animationTime);

    public static BbModelRenderer get() {
        return RptBbModelUtils.getInstance();
    }
}
