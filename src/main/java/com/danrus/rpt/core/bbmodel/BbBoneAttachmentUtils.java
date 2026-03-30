package com.danrus.rpt.core.bbmodel;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.animation.AnimationBlendState;
import com.danrus.bb4j.model.geometry.Element;
import com.danrus.bb4j.model.outliner.OutlinerNode;
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

    public static boolean withBoneByUuidAndGroupElementCenter(
            BbModelDocument model,
            PoseStack poseStack,
            String boneUuid,
            @Nullable List<AnimationBlendState> activeAnimations,
            Runnable renderCall
    ) {
        return withBoneByUuidAndGroupElementCenter(model, poseStack, boneUuid, activeAnimations, 1.0f, renderCall);
    }

    public static boolean withBoneByUuidAndGroupElementCenter(
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
            applyFirstElementAnchorTransform(model, boneUuid, poseStack);
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

    public static boolean withBoneByUuidAndGroupElementCenter(
            BbModelDocument model,
            PoseStack poseStack,
            String boneUuid,
            @Nullable FsmInstance fsmInstance,
            Runnable renderCall
    ) {
        List<AnimationBlendState> activeAnimations = fsmInstance != null ? fsmInstance.getBlendStates() : null;
        return withBoneByUuidAndGroupElementCenter(model, poseStack, boneUuid, activeAnimations, renderCall);
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

    private static void applyFirstElementAnchorTransform(BbModelDocument model, String boneUuid, PoseStack poseStack) {
        OutlinerNode groupNode = findGroupNodeByUuid(model.getOutliner(), boneUuid);
        if (groupNode == null) {
            return;
        }

        String elementUuid = findFirstElementUuid(groupNode);
        if (elementUuid == null || elementUuid.isBlank()) {
            return;
        }

        Element element = model.findElementByUuid(elementUuid);
        if (element == null) {
            return;
        }

        Double[] from = element.getFrom();
        Double[] to = element.getTo();
        if (from != null && to != null && from.length >= 3 && to.length >= 3) {
            float centerX = (float) ((from[0] + to[0]) / 32.0);
            float centerY = (float) ((from[1] + to[1]) / 32.0);
            float centerZ = (float) ((from[2] + to[2]) / 32.0);
            poseStack.translate(centerX, centerY, centerZ);
        }

        Double[] rotation = element.getRotation();
        if (rotation == null || rotation.length < 3) {
            return;
        }

        float rotX = rotation[0] != null ? rotation[0].floatValue() : 0.0f;
        float rotY = rotation[1] != null ? rotation[1].floatValue() : 0.0f;
        float rotZ = rotation[2] != null ? rotation[2].floatValue() : 0.0f;
        if (Math.abs(rotX) <= 0.0001f && Math.abs(rotY) <= 0.0001f && Math.abs(rotZ) <= 0.0001f) {
            return;
        }

        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rotZ));
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotY));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(rotX));
    }

    private static @Nullable OutlinerNode findGroupNodeByUuid(@Nullable List<OutlinerNode> nodes, String targetUuid) {
        if (nodes == null || nodes.isEmpty()) {
            return null;
        }

        for (OutlinerNode node : nodes) {
            if (node == null) {
                continue;
            }
            if (node.isGroup() && targetUuid.equals(node.getUuid())) {
                return node;
            }
            OutlinerNode childMatch = findGroupNodeByUuid(node.getChildren(), targetUuid);
            if (childMatch != null) {
                return childMatch;
            }
        }
        return null;
    }

    private static @Nullable String findFirstElementUuid(@Nullable OutlinerNode root) {
        if (root == null) {
            return null;
        }

        List<OutlinerNode> children = root.getChildren();
        if (children == null || children.isEmpty()) {
            return null;
        }

        for (OutlinerNode child : children) {
            if (child != null && !child.isGroup() && child.getUuid() != null && !child.getUuid().isBlank()) {
                return child.getUuid();
            }
        }

        for (OutlinerNode child : children) {
            String nested = findFirstElementUuid(child);
            if (nested != null && !nested.isBlank()) {
                return nested;
            }
        }
        return null;
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
