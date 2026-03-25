package com.danrus.rpt.core.bbmodel.baked;

import com.danrus.bb4j.api.utils.TextureUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.danrus.rpt.core.bbmodel.RptBbModelUtils.TextureRenderData;

import java.util.ArrayList;

public class BakedModelRenderer {
    
    private static final ThreadLocal<ArrayList<TranslucentQuad>> TRANSLUCENT_QUAD_CACHE = ThreadLocal.withInitial(ArrayList::new);
    
    public static void render(BakedModelData modelData, MultiBufferSource bufferSource, PoseStack.Pose pose, 
                       int packedLight, int packedOverlay, 
                       TextureResolver textureResolver) {
                       
        ArrayList<TranslucentQuad> translucentFaces = TRANSLUCENT_QUAD_CACHE.get();
        translucentFaces.clear();
        
        renderOpaque(modelData, bufferSource, pose, packedLight, packedOverlay, textureResolver, translucentFaces);
        renderTranslucent(translucentFaces, bufferSource, packedLight, packedOverlay);
    }

    public static void renderOpaque(BakedModelData modelData, MultiBufferSource bufferSource, PoseStack.Pose pose, 
                       int packedLight, int packedOverlay, 
                       TextureResolver textureResolver, java.util.List<TranslucentQuad> outTranslucentFaces) {
        
        PoseStack poseStack = new PoseStack();
        poseStack.last().pose().set(pose.pose());
        poseStack.last().normal().set(pose.normal());
        
        Minecraft minecraft = Minecraft.getInstance();
        double cameraX = 0.0;
        double cameraY = 0.0;
        double cameraZ = 0.0;
        boolean hasCamera = minecraft.gameRenderer != null && minecraft.gameRenderer.getMainCamera() != null;
        if (hasCamera) {
            //? <=1.21.10 {
            /*var cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();
            cameraX = cameraPos.x;
            cameraY = cameraPos.y;
            cameraZ = cameraPos.z;
            *///?} else {
            var cameraPos = minecraft.gameRenderer.getMainCamera();
            cameraX = cameraPos.position().x(); // We need valid cam position
            cameraY = cameraPos.position().y();
            cameraZ = cameraPos.position().z();
            //?}
        }
        
        for (RptBakedMesh mesh : modelData.staticMeshes()) {
            poseStack.pushPose();
            
            poseStack.translate(mesh.posX(), mesh.posY(), mesh.posZ());
            
            if (mesh.hasRotation() || mesh.hasScale()) {
                poseStack.translate(mesh.originX(), mesh.originY(), mesh.originZ());
                
                if (mesh.hasRotation()) {
                    poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(mesh.rotZ()));
                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(mesh.rotY()));
                    poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(mesh.rotX()));
                }
                
                if (mesh.hasScale()) {
                    poseStack.scale(mesh.scaleX(), mesh.scaleY(), mesh.scaleZ());
                }
                
                poseStack.translate(-mesh.originX(), -mesh.originY(), -mesh.originZ());
            }
            
            Matrix4f matrix4f = poseStack.last().pose();
            Matrix3f normalMatrix = poseStack.last().normal();
            Matrix4f translucentPoseMatrix = null;
            Matrix3f translucentNormalMatrix = null;
            
            for (RptBakedQuad quad : mesh.quads()) {
                TextureRenderData textureData = textureResolver.resolve(quad.textureReference());
                
                if (textureData.alphaMode() == TextureUtils.AlphaMode.TRANSLUCENT) {
                    if (outTranslucentFaces != null) {
                        if (translucentPoseMatrix == null) {
                            translucentPoseMatrix = new Matrix4f(matrix4f);
                            translucentNormalMatrix = new Matrix3f(normalMatrix);
                        }
                        outTranslucentFaces.add(new TranslucentQuad(
                                quad,
                                textureData,
                                translucentPoseMatrix,
                                translucentNormalMatrix,
                                computeQuadDepth(quad, translucentPoseMatrix, hasCamera, cameraX, cameraY, cameraZ)
                        ));
                    }
                } else {
                    renderQuad(quad, bufferSource.getBuffer(textureData.renderType()), matrix4f, normalMatrix, packedLight, packedOverlay);
                }
            }
            
            poseStack.popPose();
        }
    }

    public static void renderDynamic(BakedModelData modelData, MultiBufferSource bufferSource, PoseStack.Pose pose, 
                       int packedLight, int packedOverlay, 
                       TextureResolver textureResolver, java.util.Map<String, com.danrus.bb4j.api.utils.TransformUtils.Transform> animatedTransforms) {
                       
        ArrayList<TranslucentQuad> translucentFaces = TRANSLUCENT_QUAD_CACHE.get();
        translucentFaces.clear();
        
        renderDynamicOpaque(modelData, bufferSource, pose, packedLight, packedOverlay, textureResolver, translucentFaces, animatedTransforms);
        renderTranslucent(translucentFaces, bufferSource, packedLight, packedOverlay);
    }
    
    public static void renderDynamicOpaque(BakedModelData modelData, MultiBufferSource bufferSource, PoseStack.Pose pose, 
                       int packedLight, int packedOverlay, 
                       TextureResolver textureResolver, java.util.List<TranslucentQuad> outTranslucentFaces,
                       java.util.Map<String, com.danrus.bb4j.api.utils.TransformUtils.Transform> animatedTransforms) {
        
        PoseStack poseStack = new PoseStack();
        poseStack.last().pose().set(pose.pose());
        poseStack.last().normal().set(pose.normal());
        
        Minecraft minecraft = Minecraft.getInstance();
        double cameraX = 0.0;
        double cameraY = 0.0;
        double cameraZ = 0.0;
        boolean hasCamera = minecraft.gameRenderer != null && minecraft.gameRenderer.getMainCamera() != null;
        if (hasCamera) {
            //? <=1.21.10 {
            /*var cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();
            cameraX = cameraPos.x;
            cameraY = cameraPos.y;
            cameraZ = cameraPos.z;
            *///?} else {
            var cameraPos = minecraft.gameRenderer.getMainCamera();
            cameraX = cameraPos.position().x();
            cameraY = cameraPos.position().y();
            cameraZ = cameraPos.position().z();
            //?}
        }
        
        for (RptBakedMesh mesh : modelData.staticMeshes()) {
            poseStack.pushPose();
            
            float animX = 0, animY = 0, animZ = 0;
            float animRotX = 0, animRotY = 0, animRotZ = 0;
            float animScaleX = 1, animScaleY = 1, animScaleZ = 1;
            
            if (mesh.hierarchy() != null && animatedTransforms != null) {
                for (String uuid : mesh.hierarchy()) {
                    com.danrus.bb4j.api.utils.TransformUtils.Transform t = animatedTransforms.get(uuid);
                    if (t != null) {
                        animX += t.getX();
                        animY += t.getY();
                        animZ += t.getZ();
                        animRotX += t.getRotX();
                        animRotY += t.getRotY();
                        animRotZ += t.getRotZ();
                        animScaleX *= t.getScaleX();
                        animScaleY *= t.getScaleY();
                        animScaleZ *= t.getScaleZ();
                    }
                }
            }
            
            float finalPosX = mesh.posX() + (animX / 16.0f);
            float finalPosY = mesh.posY() + (animY / 16.0f);
            float finalPosZ = mesh.posZ() + (animZ / 16.0f);
            
            float finalRotX = mesh.rotX() + animRotX;
            float finalRotY = mesh.rotY() + animRotY;
            float finalRotZ = mesh.rotZ() + animRotZ;
            
            float finalScaleX = mesh.scaleX() * animScaleX;
            float finalScaleY = mesh.scaleY() * animScaleY;
            float finalScaleZ = mesh.scaleZ() * animScaleZ;
            
            poseStack.translate(finalPosX, finalPosY, finalPosZ);
            
            boolean hasAnyRot = Math.abs(finalRotX) > 0.0001f || Math.abs(finalRotY) > 0.0001f || Math.abs(finalRotZ) > 0.0001f;
            boolean hasAnyScale = Math.abs(finalScaleX - 1.0f) > 0.0001f || Math.abs(finalScaleY - 1.0f) > 0.0001f || Math.abs(finalScaleZ - 1.0f) > 0.0001f;
            
            if (hasAnyRot || hasAnyScale) {
                poseStack.translate(mesh.originX(), mesh.originY(), mesh.originZ());
                
                if (hasAnyRot) {
                    poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(finalRotZ));
                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(finalRotY));
                    poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(finalRotX));
                }
                
                if (hasAnyScale) {
                    poseStack.scale(finalScaleX, finalScaleY, finalScaleZ);
                }
                
                poseStack.translate(-mesh.originX(), -mesh.originY(), -mesh.originZ());
            }
            
            Matrix4f matrix4f = poseStack.last().pose();
            Matrix3f normalMatrix = poseStack.last().normal();
            Matrix4f translucentPoseMatrix = null;
            Matrix3f translucentNormalMatrix = null;
            
            for (RptBakedQuad quad : mesh.quads()) {
                TextureRenderData textureData = textureResolver.resolve(quad.textureReference());
                
                if (textureData.alphaMode() == TextureUtils.AlphaMode.TRANSLUCENT) {
                    if (outTranslucentFaces != null) {
                        if (translucentPoseMatrix == null) {
                            translucentPoseMatrix = new Matrix4f(matrix4f);
                            translucentNormalMatrix = new Matrix3f(normalMatrix);
                        }
                        outTranslucentFaces.add(new TranslucentQuad(
                                quad,
                                textureData,
                                translucentPoseMatrix,
                                translucentNormalMatrix,
                                computeQuadDepth(quad, translucentPoseMatrix, hasCamera, cameraX, cameraY, cameraZ)
                        ));
                    }
                } else {
                    renderQuad(quad, bufferSource.getBuffer(textureData.renderType()), matrix4f, normalMatrix, packedLight, packedOverlay);
                }
            }
            
            poseStack.popPose();
        }
    }

    public static void renderTranslucent(java.util.List<TranslucentQuad> translucentFaces, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (translucentFaces == null || translucentFaces.isEmpty()) {
            return;
        }
        
        translucentFaces.sort((a, b) -> Double.compare(b.depth(), a.depth()));
        for (TranslucentQuad quadData : translucentFaces) {
            renderQuad(
                    quadData.quad(),
                    bufferSource.getBuffer(quadData.textureData().renderType()),
                    quadData.poseMatrix(),
                    quadData.normalMatrix(),
                    packedLight,
                    packedOverlay
            );
        }
    }
    
    private static void renderQuad(RptBakedQuad quad, VertexConsumer consumer, Matrix4f matrix4f, Matrix3f normalMatrix, int packedLight, int packedOverlay) {
        float tnx = normalMatrix.m00() * quad.nx() + normalMatrix.m10() * quad.ny() + normalMatrix.m20() * quad.nz();
        float tny = normalMatrix.m01() * quad.nx() + normalMatrix.m11() * quad.ny() + normalMatrix.m21() * quad.nz();
        float tnz = normalMatrix.m02() * quad.nx() + normalMatrix.m12() * quad.ny() + normalMatrix.m22() * quad.nz();
        
        // V0
        float tX0 = matrix4f.m00() * quad.x0() + matrix4f.m10() * quad.y0() + matrix4f.m20() * quad.z0() + matrix4f.m30();
        float tY0 = matrix4f.m01() * quad.x0() + matrix4f.m11() * quad.y0() + matrix4f.m21() * quad.z0() + matrix4f.m31();
        float tZ0 = matrix4f.m02() * quad.x0() + matrix4f.m12() * quad.y0() + matrix4f.m22() * quad.z0() + matrix4f.m32();
        consumer.addVertex(tX0, tY0, tZ0).setColor(255, 255, 255, 255).setUv(quad.u0(), quad.v0())
                .setOverlay(packedOverlay).setLight(packedLight).setNormal(tnx, tny, tnz);
                
        // V1
        float tX1 = matrix4f.m00() * quad.x1() + matrix4f.m10() * quad.y1() + matrix4f.m20() * quad.z1() + matrix4f.m30();
        float tY1 = matrix4f.m01() * quad.x1() + matrix4f.m11() * quad.y1() + matrix4f.m21() * quad.z1() + matrix4f.m31();
        float tZ1 = matrix4f.m02() * quad.x1() + matrix4f.m12() * quad.y1() + matrix4f.m22() * quad.z1() + matrix4f.m32();
        consumer.addVertex(tX1, tY1, tZ1).setColor(255, 255, 255, 255).setUv(quad.u1(), quad.v1())
                .setOverlay(packedOverlay).setLight(packedLight).setNormal(tnx, tny, tnz);
                
        // V2
        float tX2 = matrix4f.m00() * quad.x2() + matrix4f.m10() * quad.y2() + matrix4f.m20() * quad.z2() + matrix4f.m30();
        float tY2 = matrix4f.m01() * quad.x2() + matrix4f.m11() * quad.y2() + matrix4f.m21() * quad.z2() + matrix4f.m31();
        float tZ2 = matrix4f.m02() * quad.x2() + matrix4f.m12() * quad.y2() + matrix4f.m22() * quad.z2() + matrix4f.m32();
        consumer.addVertex(tX2, tY2, tZ2).setColor(255, 255, 255, 255).setUv(quad.u2(), quad.v2())
                .setOverlay(packedOverlay).setLight(packedLight).setNormal(tnx, tny, tnz);
                
        // V3
        float tX3 = matrix4f.m00() * quad.x3() + matrix4f.m10() * quad.y3() + matrix4f.m20() * quad.z3() + matrix4f.m30();
        float tY3 = matrix4f.m01() * quad.x3() + matrix4f.m11() * quad.y3() + matrix4f.m21() * quad.z3() + matrix4f.m31();
        float tZ3 = matrix4f.m02() * quad.x3() + matrix4f.m12() * quad.y3() + matrix4f.m22() * quad.z3() + matrix4f.m32();
        consumer.addVertex(tX3, tY3, tZ3).setColor(255, 255, 255, 255).setUv(quad.u3(), quad.v3())
                .setOverlay(packedOverlay).setLight(packedLight).setNormal(tnx, tny, tnz);
    }
    
    private static double computeQuadDepth(RptBakedQuad quad, Matrix4f poseMatrix, boolean hasCamera, double cameraX, double cameraY, double cameraZ) {
        float transformedX = poseMatrix.m00() * quad.centerX() + poseMatrix.m10() * quad.centerY() + poseMatrix.m20() * quad.centerZ() + poseMatrix.m30();
        float transformedY = poseMatrix.m01() * quad.centerX() + poseMatrix.m11() * quad.centerY() + poseMatrix.m21() * quad.centerZ() + poseMatrix.m31();
        float transformedZ = poseMatrix.m02() * quad.centerX() + poseMatrix.m12() * quad.centerY() + poseMatrix.m22() * quad.centerZ() + poseMatrix.m32();

        if (hasCamera) {
            double dx = transformedX - cameraX;
            double dy = transformedY - cameraY;
            double dz = transformedZ - cameraZ;
            return dx * dx + dy * dy + dz * dz;
        }
        return transformedZ;
    }
    
    public interface TextureResolver {
        TextureRenderData resolve(String textureReference);
    }

    public record TranslucentQuad(RptBakedQuad quad, TextureRenderData textureData, Matrix4f poseMatrix, Matrix3f normalMatrix, double depth) {}
}
