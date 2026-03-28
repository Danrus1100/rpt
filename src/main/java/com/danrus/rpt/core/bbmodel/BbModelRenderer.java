package com.danrus.rpt.core.bbmodel;

import com.danrus.bb4j.api.utils.RenderUtils;
import com.danrus.bb4j.api.utils.TextureUtils;
import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.animation.AnimationBlendState;
import com.danrus.bb4j.model.texture.Texture;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public interface BbModelRenderer {
    void renderToBuffer(BbModelDocument model, MultiBufferSource bufferSource, PoseStack poseStack, int packedLight, int packedOverlay);
    void renderToBuffer(BbModelDocument model, MultiBufferSource bufferSource, PoseStack poseStack, int packedLight, int packedOverlay, @Nullable LivingEntity holder);
    void renderToBuffer(BbModelDocument model, MultiBufferSource bufferSource, PoseStack poseStack, int packedLight, int packedOverlay, List<AnimationBlendState> activeAnimations, @Nullable LivingEntity holder);
    void renderDynamicToBuffer(BbModelDocument model, MultiBufferSource bufferSource, PoseStack.Pose pose, int packedLight, int packedOverlay, List<AnimationBlendState> activeAnimations, @Nullable Identifier playerSkin);
    void renderToBuffer(BbModelDocument model, MultiBufferSource bufferSource, PoseStack poseStack, int packedLight, int packedOverlay, String animation, double animationTime, @Nullable LivingEntity holder);
    void renderStaticToBuffer(BbModelDocument model, MultiBufferSource bufferSource, PoseStack.Pose pose, int packedLight, int packedOverlay, @Nullable Identifier playerSkin);

    // Split rendering methods
    void renderStaticOpaque(BbModelDocument model, MultiBufferSource bufferSource, PoseStack.Pose pose, int packedLight, int packedOverlay, @Nullable Identifier playerSkin, List<com.danrus.rpt.core.bbmodel.baked.BakedModelRenderer.TranslucentQuad> outTranslucent);
    void renderStaticTranslucent(List<com.danrus.rpt.core.bbmodel.baked.BakedModelRenderer.TranslucentQuad> quads, MultiBufferSource bufferSource, int packedLight, int packedOverlay, @Nullable Identifier playerSkin);

    void getExtentsForGui(BbModelDocument model, PoseStack poseStack, //? <=1.21.10
                          //Set<Vector3f>
                          //? >=1.21.11
                          Consumer<Vector3fc>
                                  output);
    void getExtentsForGui(BbModelDocument model, PoseStack poseStack,
                          //? <=1.21.10
                          //Set<Vector3f>
                          //? >=1.21.11
                          Consumer<Vector3fc>
                                  output, String animation, double animationTime);

    boolean applyBoneTransform(BbModelDocument model, PoseStack poseStack, String boneName, @Nullable List<AnimationBlendState> activeAnimations);
    boolean applyBoneTransformByUuid(BbModelDocument model, PoseStack poseStack, String boneUuid, @Nullable List<AnimationBlendState> activeAnimations);

    public static BbModelRenderer get() {
        return RptBbModelUtils.getInstance();
    }
}
