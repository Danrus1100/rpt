package com.danrus.rpt.core.bbmodel;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.animation.AnimationBlendState;
import com.danrus.rpt.core.bbmodel.fsm.FsmInstance;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public final class BbBoneAttachmentUtils {

    private BbBoneAttachmentUtils() {}

    public static boolean withBoneByName(
            BbModelDocument model,
            PoseStack poseStack,
            String boneName,
            @Nullable List<AnimationBlendState> activeAnimations,
            Runnable renderCall
    ) {
        return withBoneByName(model, poseStack, boneName, activeAnimations, 1.0f, renderCall);
    }

    public static boolean withBoneByName(
            BbModelDocument model,
            PoseStack poseStack,
            String boneName,
            @Nullable List<AnimationBlendState> activeAnimations,
            float blendProgress,
            Runnable renderCall
    ) {
        if (model == null || poseStack == null || boneName == null || boneName.isBlank() || renderCall == null) {
            return false;
        }

        float alpha = clamp01(blendProgress);
        poseStack.pushPose();
        Matrix4f startPose = new Matrix4f(poseStack.last().pose());

        boolean attached = BbModelRenderer.get().applyBoneTransform(model, poseStack, boneName, activeAnimations);
        if (attached) {
            if (alpha < 0.9999f) {
                Matrix4f targetPose = new Matrix4f(poseStack.last().pose());
                Matrix4f blendedPose = interpolatePose(startPose, targetPose, alpha);
                poseStack.last().pose().set(blendedPose);
                Matrix3f blendedNormal = blendedPose.normal(new Matrix3f());
                poseStack.last().normal().set(blendedNormal);
            }
            renderCall.run();
        }
        poseStack.popPose();
        return attached;
    }

    public static boolean withBoneByUuid(
            BbModelDocument model,
            PoseStack poseStack,
            String boneUuid,
            @Nullable List<AnimationBlendState> activeAnimations,
            Runnable renderCall
    ) {
        return withBoneByUuid(model, poseStack, boneUuid, activeAnimations, 1.0f, renderCall);
    }

    public static boolean withBoneByUuid(
            BbModelDocument model,
            PoseStack poseStack,
            String boneUuid,
            @Nullable List<AnimationBlendState> activeAnimations,
            float blendProgress,
            Runnable renderCall
    ) {
        if (model == null || poseStack == null || boneUuid == null || boneUuid.isBlank() || renderCall == null) {
            return false;
        }

        float alpha = clamp01(blendProgress);
        poseStack.pushPose();
        Matrix4f startPose = new Matrix4f(poseStack.last().pose());

        boolean attached = BbModelRenderer.get().applyBoneTransformByUuid(model, poseStack, boneUuid, activeAnimations);
        if (attached) {
            if (alpha < 0.9999f) {
                Matrix4f targetPose = new Matrix4f(poseStack.last().pose());
                Matrix4f blendedPose = interpolatePose(startPose, targetPose, alpha);
                poseStack.last().pose().set(blendedPose);
                Matrix3f blendedNormal = blendedPose.normal(new Matrix3f());
                poseStack.last().normal().set(blendedNormal);
            }
            renderCall.run();
        }
        poseStack.popPose();
        return attached;
    }

    public static boolean withBoneByName(
            BbModelDocument model,
            PoseStack poseStack,
            String boneName,
            @Nullable FsmInstance fsmInstance,
            Runnable renderCall
    ) {
        List<AnimationBlendState> activeAnimations = fsmInstance != null ? fsmInstance.getBlendStates() : null;
        return withBoneByName(model, poseStack, boneName, activeAnimations, renderCall);
    }

    public static boolean withBoneByNameFromAnimationTail(
            BbModelDocument model,
            PoseStack poseStack,
            String boneName,
            @Nullable FsmInstance fsmInstance,
            float startProgress,
            Runnable renderCall
    ) {
        List<AnimationBlendState> activeAnimations = fsmInstance != null ? fsmInstance.getBlendStates() : null;
        float blendProgress = fsmInstance != null ? fsmInstance.getCurrentAnimationProgressFrom(startProgress, model) : 1.0f;
        return withBoneByName(model, poseStack, boneName, activeAnimations, blendProgress, renderCall);
    }

    public static boolean withBoneByName(
            BbModelDocument model,
            PoseStack poseStack,
            String boneName,
            @Nullable FsmInstance fsmInstance,
            float blendProgress,
            Runnable renderCall
    ) {
        List<AnimationBlendState> activeAnimations = fsmInstance != null ? fsmInstance.getBlendStates() : null;
        return withBoneByName(model, poseStack, boneName, activeAnimations, blendProgress, renderCall);
    }

    public static boolean withBoneByUuid(
            BbModelDocument model,
            PoseStack poseStack,
            String boneUuid,
            @Nullable FsmInstance fsmInstance,
            Runnable renderCall
    ) {
        List<AnimationBlendState> activeAnimations = fsmInstance != null ? fsmInstance.getBlendStates() : null;
        return withBoneByUuid(model, poseStack, boneUuid, activeAnimations, renderCall);
    }

    public static boolean withBoneByUuidFromAnimationTail(
            BbModelDocument model,
            PoseStack poseStack,
            String boneUuid,
            @Nullable FsmInstance fsmInstance,
            float startProgress,
            Runnable renderCall
    ) {
        List<AnimationBlendState> activeAnimations = fsmInstance != null ? fsmInstance.getBlendStates() : null;
        float blendProgress = fsmInstance != null ? fsmInstance.getCurrentAnimationProgressFrom(startProgress, model) : 1.0f;
        return withBoneByUuid(model, poseStack, boneUuid, activeAnimations, blendProgress, renderCall);
    }

    public static boolean withBoneByUuid(
            BbModelDocument model,
            PoseStack poseStack,
            String boneUuid,
            @Nullable FsmInstance fsmInstance,
            float blendProgress,
            Runnable renderCall
    ) {
        List<AnimationBlendState> activeAnimations = fsmInstance != null ? fsmInstance.getBlendStates() : null;
        return withBoneByUuid(model, poseStack, boneUuid, activeAnimations, blendProgress, renderCall);
    }

    public static float approach(float current, float target, float speedPerSecond, float deltaSeconds) {
        if (deltaSeconds <= 0.0f) {
            return current;
        }
        if (speedPerSecond <= 0.0f) {
            return current;
        }

        float step = speedPerSecond * deltaSeconds;
        float diff = target - current;
        if (Math.abs(diff) <= step) {
            return target;
        }
        return current + Math.copySign(step, diff);
    }

    public static float approach01ByDuration(float current, float target, float deltaSeconds, float durationSeconds) {
        if (durationSeconds <= 0.0f) {
            return clamp01(target);
        }
        float speed = 1.0f / durationSeconds;
        return clamp01(approach(clamp01(current), clamp01(target), speed, deltaSeconds));
    }

    public static float approachToBone(float current, float deltaSeconds, float durationSeconds) {
        return approach01ByDuration(current, 1.0f, deltaSeconds, durationSeconds);
    }

    public static float approachFromBone(float current, float deltaSeconds, float durationSeconds) {
        return approach01ByDuration(current, 0.0f, deltaSeconds, durationSeconds);
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }

    private static Matrix4f interpolatePose(Matrix4f start, Matrix4f end, float alpha) {
        if (alpha <= 0.0001f) {
            return new Matrix4f(start);
        }
        if (alpha >= 0.9999f) {
            return new Matrix4f(end);
        }

        Vector3f startTranslation = start.getTranslation(new Vector3f());
        Vector3f endTranslation = end.getTranslation(new Vector3f());
        Vector3f blendedTranslation = new Vector3f(startTranslation).lerp(endTranslation, alpha);

        Quaternionf startRotation = start.getUnnormalizedRotation(new Quaternionf()).normalize();
        Quaternionf endRotation = end.getUnnormalizedRotation(new Quaternionf()).normalize();
        Quaternionf blendedRotation = new Quaternionf(startRotation).slerp(endRotation, alpha).normalize();

        Vector3f startScale = start.getScale(new Vector3f());
        Vector3f endScale = end.getScale(new Vector3f());
        Vector3f blendedScale = new Vector3f(startScale).lerp(endScale, alpha);

        return new Matrix4f()
                .identity()
                .translate(blendedTranslation)
                .rotate(blendedRotation)
                .scale(blendedScale);
    }
}
