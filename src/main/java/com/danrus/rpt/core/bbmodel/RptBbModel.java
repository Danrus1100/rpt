package com.danrus.rpt.core.bbmodel;

import com.danrus.bb4j.api.utils.RenderUtils;
import com.danrus.bb4j.api.utils.TextureUtils;
import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.project.Resolution;
import com.danrus.bb4j.model.texture.Texture;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RptBbModel {

    private static final Logger log = LoggerFactory.getLogger(RptBbModel.class);

    public static void renderToBuffer(BbModelDocument model, MultiBufferSource bufferSource, PoseStack poseStack) {
        TextureUtils utils = TextureUtils.forDocument(model);
        List<Texture> textures = utils.getAllTextures();
        int textureWidth = model.getResolution().getWidth();
        int textureHeight = model.getResolution().getHeight();

        renderToBuffer(bufferSource, poseStack, RenderUtils.forDocument(model).getAllMeshes(), textures, textureWidth, textureHeight);
    }

    public static void renderToBuffer(BbModelDocument model, MultiBufferSource bufferSource, PoseStack poseStack, String animation, double animationTime) {
        TextureUtils utils = TextureUtils.forDocument(model);
        List<Texture> textures = utils.getAllTextures();
        int textureWidth = model.getResolution().getWidth();
        int textureHeight = model.getResolution().getHeight();

        renderToBuffer(bufferSource, poseStack, RenderUtils.forDocument(model).getMeshesAtAnimationTime(animation, animationTime), textures, textureWidth, textureHeight);
    }

    private static void renderToBuffer(MultiBufferSource bufferSource, PoseStack poseStack, List<RenderUtils.RenderableMesh> meshes, List<Texture> textures, int textureWidth, int textureHeight) {
        for (RenderUtils.RenderableMesh mesh: meshes) {
            ResourceLocation textureLocation = getTextureLocation(mesh.getTextureUuid(), textures);
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.entitySolid(textureLocation));

            for (RenderUtils.RenderableFace face : mesh.getFaces()) {
                renderFace(face, consumer, poseStack, textureWidth, textureHeight);
            }
        }
    }

    public static void registerModelTextures(BbModelDocument model) {
        TextureUtils utils = TextureUtils.forDocument(model);
        utils.getAllTextures().forEach(texture -> {
            try {
                NativeImage image = NativeImage.read(utils.loadTextureData(texture));
                Minecraft.getInstance().submit(() -> {
                    try {
                        DynamicTexture mcTexture = new DynamicTexture(() -> "Rpt BbModel Texture " + texture.getName(), image);
                        Minecraft.getInstance().getTextureManager().register(getTextureLocation(texture), mcTexture);
                    } catch (Exception e) {
                        log.error("Error registering texture {}", texture.getName(), e);
                    }
                });
            } catch (IOException e) {
                log.error("Error loading bbmodel texture {} from model {}: ", texture.getName(), model.getMeta().getName(), e);
            }
        });
    }

    public static void getExtentsForGui(BbModelDocument model, PoseStack poseStack, Set<Vector3f> output) {
        getExtentsForGui(model, poseStack, output, null, 0);
    }

    public static void getExtentsForGui(BbModelDocument model, PoseStack poseStack, Set<Vector3f> output, String animation, double animationTime) {
        RenderUtils render = RenderUtils.forDocument(model);
        List<RenderUtils.RenderableMesh> meshes = (animation != null)
                ? render.getMeshesAtAnimationTime(animation, animationTime)
                : render.getAllMeshes();

        for (RenderUtils.RenderableMesh mesh : meshes) {
            for (RenderUtils.RenderableFace face : mesh.getFaces()) {
                for (double[] vertex : face.getVertices()) {
                    float x = (float) vertex[0];
                    float y = (float) vertex[1];
                    float z = (float) vertex[2];

                    Vector3f transformedVertex = poseStack.last().pose().transformPosition(x, y, z, new Vector3f());
                    output.add(transformedVertex);
                }
            }
        }
    }

    public static ResourceLocation getTextureLocation(Texture texture) {
        return getTextureLocation(texture.getUuid(), null);
    }

    public static ResourceLocation getTextureLocation(String uuid, @Nullable List<Texture> textures) {
        if (textures == null)  {
            return ResourceLocation.fromNamespaceAndPath(
                    "rpt",
                    "bbmodel/texture/" + uuid
            );
        }
        try {
            return ResourceLocation.fromNamespaceAndPath(
                    "rpt",
                    "bbmodel/texture/" + textures.get(Integer.parseInt(uuid)).getUuid()
            );
        } catch (Exception e) {
            return ResourceLocation.fromNamespaceAndPath(
                    "rpt",
                    "bbmodel/texture/" + uuid
            );
        }
    }

    private static void renderFace(RenderUtils.RenderableFace face, VertexConsumer consumer, PoseStack poseStack, int textureWidth, int textureHeight) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Vector3f vector3f = new Vector3f();

        float[] normal = face.getNormal() != null
                ? new float[]{(float)face.getNormal()[0], (float)face.getNormal()[1], (float)face.getNormal()[2]}
                : calculateNormal(face.getVertices());

        Vector3f transformedNormal = pose.transformNormal(normal[0], normal[1], normal[2], vector3f);

        double[] uvBounds = face.getUv();
        double u1 = uvBounds != null && uvBounds.length > 0 ? uvBounds[0] / textureWidth : 0;
        double v1 = uvBounds != null && uvBounds.length > 1 ? uvBounds[1] / textureHeight : 0;
        double u2 = uvBounds != null && uvBounds.length > 2 ? uvBounds[2] / textureWidth : 1;
        double v2 = uvBounds != null && uvBounds.length > 3 ? uvBounds[3] / textureHeight : 1;

        int color = 0xFFFFFFFF;

        int packedLight = LightTexture.pack(15, 15);
        int packedOverlay = 0;

        double[][] vertices = face.getVertices();

        float[][] uvs = new float[vertices.length][2];
        if (vertices.length == 4) {
            uvs[0] = new float[]{(float)u1, (float)v1};
            uvs[1] = new float[]{(float)u2, (float)v1};
            uvs[2] = new float[]{(float)u2, (float)v2};
            uvs[3] = new float[]{(float)u1, (float)v2};
        }

        for (int i = 0; i < vertices.length; i++) {
            double[] vertex = vertices[i];
            float u = uvs[i][0];
            float v = uvs[i][1];

            Vector3f transformedPos = matrix4f.transformPosition(
                    (float)vertex[0] / 16.0f,
                    (float)vertex[1] / 16.0f,
                    (float)vertex[2] / 16.0f,
                    vector3f
            );

            consumer.addVertex(transformedPos.x(), transformedPos.y(), transformedPos.z())
                    .setColor(255, 255, 255, 0)
                    .setUv(u, v)
                    .setOverlay(packedOverlay)
                    .setLight(packedLight)
                    .setNormal(transformedNormal.x(), transformedNormal.y(), transformedNormal.z());
        }
    }

    private static float[] calculateNormal(double[][] vertices) {
        if (vertices.length < 3) {
            return new float[]{0, 1, 0};
        }

        double[] v1 = vertices[0];
        double[] v2 = vertices[1];
        double[] v3 = vertices[2];

        float x = (float)((v2[1]-v1[1])*(v3[2]-v1[2]) - (v2[2]-v1[2])*(v3[1]-v1[1]));
        float y = (float)((v2[2]-v1[2])*(v3[0]-v1[0]) - (v2[0]-v1[0])*(v3[2]-v1[2]));
        float z = (float)((v2[0]-v1[0])*(v3[1]-v1[1]) - (v2[1]-v1[1])*(v3[0]-v1[0]));

        float length = (float)Math.sqrt(x*x + y*y + z*z);
        if (length > 0) {
            x /= length;
            y /= length;
            z /= length;
        }

        return new float[]{x, y, z};
    }
}
