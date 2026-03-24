package com.danrus.rpt.core.bbmodel;

import com.danrus.bb4j.api.utils.RenderUtils;
import com.danrus.bb4j.api.utils.TextureUtils;
import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.bb4j.model.animation.AnimationBlendState;
import com.danrus.bb4j.model.texture.Texture;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class RptBbModelUtils implements BbModelRenderer {

    private static final RptBbModelUtils INSTANCE = new RptBbModelUtils();

    private RptBbModelUtils() {}

    public static RptBbModelUtils getInstance() {
        return INSTANCE;
    }

    private static final Logger log = LoggerFactory.getLogger(RptBbModelUtils.class);
    private static final Map<ResourceLocation, TextureUtils.AlphaMode> TEXTURE_ALPHA_MODES = new ConcurrentHashMap<>();
    private final Map<BbModelDocument, List<RenderUtils.RenderableMesh>> STATIC_MESH_CACHE = Collections.synchronizedMap(new WeakHashMap<>());
    private final ThreadLocal<ArrayList<FaceRenderData>> TRANSLUCENT_FACE_CACHE = ThreadLocal.withInitial(ArrayList::new);
    private final ThreadLocal<HashMap<String, TextureRenderData>> TEXTURE_RENDER_DATA_CACHE = ThreadLocal.withInitial(HashMap::new);

    @Override
    public void renderToBuffer(BbModelDocument model, MultiBufferSource bufferSource, PoseStack poseStack, int packedLight, int packedOverlay) {
        renderToBuffer(model, bufferSource, poseStack, packedLight, packedOverlay, null);
    }

    public void renderToBuffer(BbModelDocument model, MultiBufferSource bufferSource, PoseStack poseStack, int packedLight, int packedOverlay, @Nullable LivingEntity holder) {
        TextureUtils utils = TextureUtils.forDocument(model);
        List<Texture> textures = utils.getAllTextures();

        List<RenderUtils.RenderableMesh> meshes = STATIC_MESH_CACHE.computeIfAbsent(
                model,
                key -> RenderUtils.forDocument(key).getAllMeshes()
        );

        renderToBuffer(bufferSource, poseStack, meshes, textures, holder instanceof LocalPlayer player ? player.getSkin().texture() : null, packedLight, packedOverlay, utils);
    }

    public void renderToBuffer(BbModelDocument model, MultiBufferSource bufferSource, PoseStack poseStack, int packedLight, int packedOverlay, List<AnimationBlendState> activeAnimations, @Nullable LivingEntity holder) {
        if (activeAnimations == null || activeAnimations.isEmpty()) {
            renderToBuffer(model, bufferSource, poseStack, packedLight, packedOverlay);
            return;
        }
        TextureUtils utils = TextureUtils.forDocument(model);
        List<Texture> textures = utils.getAllTextures();
        @Nullable ResourceLocation location = holder instanceof LocalPlayer player ? player.getSkin().texture() : null;
        
        renderToBuffer(bufferSource, poseStack, RenderUtils.forDocument(model).getBlendedMeshes(activeAnimations), textures, location, packedLight, packedOverlay, utils);
    }

    public void renderToBuffer(BbModelDocument model, MultiBufferSource bufferSource, PoseStack poseStack, int packedLight, int packedOverlay, @Nullable String animation, double animationTime, @Nullable LivingEntity holder) {
        if (animation == null || animation.isEmpty()) {
            renderToBuffer(model, bufferSource, poseStack, packedLight, packedOverlay);
            return;
        }
        TextureUtils utils = TextureUtils.forDocument(model);
        List<Texture> textures = utils.getAllTextures();
        @Nullable ResourceLocation location = holder instanceof LocalPlayer player ? player.getSkin().texture() : null;

        renderToBuffer(bufferSource, poseStack, RenderUtils.forDocument(model).getMeshesAtAnimationTime(animation, animationTime), textures, location, packedLight, packedOverlay, utils);
    }

    private void renderToBuffer(MultiBufferSource bufferSource, PoseStack poseStack, List<RenderUtils.RenderableMesh> meshes, List<Texture> textures, @Nullable ResourceLocation playerSkin, int packedLight, int packedOverlay, TextureUtils textureUtils) {
        ArrayList<FaceRenderData> translucentFaces = TRANSLUCENT_FACE_CACHE.get();
        translucentFaces.clear();
        HashMap<String, TextureRenderData> textureRenderDataCache = TEXTURE_RENDER_DATA_CACHE.get();
        textureRenderDataCache.clear();

        Minecraft minecraft = Minecraft.getInstance();
        double cameraX = 0.0;
        double cameraY = 0.0;
        double cameraZ = 0.0;
        boolean hasCamera = minecraft.gameRenderer != null && minecraft.gameRenderer.getMainCamera() != null;
        if (hasCamera) {
            var cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();
            cameraX = cameraPos.x;
            cameraY = cameraPos.y;
            cameraZ = cameraPos.z;
        }

        for (RenderUtils.RenderableMesh mesh: meshes) {
            poseStack.pushPose();

            poseStack.translate(mesh.getPosition()[0] / 16.0, mesh.getPosition()[1] / 16.0, mesh.getPosition()[2] / 16.0);

            double[] origin = mesh.getLocalOrigin() != null ? mesh.getLocalOrigin() : mesh.getLocalCenter();
            
            boolean hasRotation = Math.abs(mesh.getRotation()[0]) > 0.0001 || Math.abs(mesh.getRotation()[1]) > 0.0001 || Math.abs(mesh.getRotation()[2]) > 0.0001;
            boolean hasScale = mesh.getScale() != null && (Math.abs(mesh.getScale()[0] - 1.0) > 0.0001 || Math.abs(mesh.getScale()[1] - 1.0) > 0.0001 || Math.abs(mesh.getScale()[2] - 1.0) > 0.0001);

            if (hasRotation || hasScale) {
                poseStack.translate(origin[0] / 16.0, origin[1] / 16.0, origin[2] / 16.0);
                
                if (hasRotation) {
                    poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees((float) mesh.getRotation()[2]));
                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees((float) mesh.getRotation()[1]));
                    poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees((float) mesh.getRotation()[0]));
                }
                
                if (hasScale) {
                    poseStack.scale((float) mesh.getScale()[0], (float) mesh.getScale()[1], (float) mesh.getScale()[2]);
                }
                
                poseStack.translate(-origin[0] / 16.0, -origin[1] / 16.0, -origin[2] / 16.0);
            }

            Matrix4f translucentPoseMatrix = null;
            Matrix3f translucentNormalMatrix = null;

            for (RenderUtils.RenderableFace face : mesh.getFaces()) {
                String textureReference = face.getTextureUuid() != null ? face.getTextureUuid() : mesh.getTextureUuid();
                TextureRenderData textureData = resolveTextureRenderData(textureReference, textures, textureUtils, textureRenderDataCache, playerSkin);

                if (textureData.alphaMode() == TextureUtils.AlphaMode.TRANSLUCENT) {
                    if (translucentPoseMatrix == null) {
                        translucentPoseMatrix = new Matrix4f(poseStack.last().pose());
                        translucentNormalMatrix = new Matrix3f(poseStack.last().normal());
                    }
                    translucentFaces.add(new FaceRenderData(
                            face,
                            textureData,
                            translucentPoseMatrix,
                            translucentNormalMatrix,
                            computeFaceDepth(face, translucentPoseMatrix, hasCamera, cameraX, cameraY, cameraZ)
                    ));
                } else {
                    renderFace(face, bufferSource.getBuffer(textureData.renderType()), poseStack, packedLight, packedOverlay);
                }
            }

            poseStack.popPose();
        }

        if (!translucentFaces.isEmpty()) {
            translucentFaces.sort((a, b) -> Double.compare(b.depth(), a.depth()));
            for (FaceRenderData faceData : translucentFaces) {
                renderFace(
                        faceData.face(),
                        bufferSource.getBuffer(faceData.textureData().renderType()),
                        faceData.poseMatrix(),
                        faceData.normalMatrix(),
                        packedLight,
                        packedOverlay
                );
            }
        }
    }

    public static void registerModelTextures(BbModelDocument model) {
        TextureUtils utils = TextureUtils.forDocument(model);
        utils.getAllTextures().forEach(texture -> {
            try {
                NativeImage image = NativeImage.read(utils.loadTextureData(texture));
                TextureUtils.AlphaMode alphaMode = classifyAlphaMode(image);
                
                Minecraft.getInstance().submit(() -> {
                    try {
                        DynamicTexture mcTexture = new DynamicTexture(() -> "Rpt BbModel Texture " + texture.getName(), image);
                        ResourceLocation textureLocation = getTextureLocation(texture);
                        Minecraft.getInstance().getTextureManager().register(textureLocation, mcTexture);
                        TEXTURE_ALPHA_MODES.put(textureLocation, alphaMode);
                    } catch (Exception e) {
                        log.error("Error registering texture {}", texture.getName(), e);
                    }
                });
            } catch (IOException e) {
                log.error("Error loading bbmodel texture {} from model {}: ", texture.getName(), model.getMeta().getName(), e);
            }
        });
    }

    public void getExtentsForGui(BbModelDocument model, PoseStack poseStack, Set<Vector3f> output) {
        getExtentsForGui(model, poseStack, output, null, 0);
    }

    public void getExtentsForGui(BbModelDocument model, PoseStack poseStack, Set<Vector3f> output, String animation, double animationTime) {
        RenderUtils render = RenderUtils.forDocument(model);
        List<RenderUtils.RenderableMesh> meshes = (animation != null)
                ? render.getMeshesAtAnimationTime(animation, animationTime)
                : render.getAllMeshes();

        for (RenderUtils.RenderableMesh mesh : meshes) {
            for (RenderUtils.RenderableFace face : mesh.getFaces()) {
                for (double[] vertex : face.getVertices()) {
                    float x = (float) vertex[0] / 16.0f;
                    float y = (float) vertex[1] / 16.0f;
                    float z = (float) vertex[2] / 16.0f;

                    Vector3f transformedVertex = poseStack.last().pose().transformPosition(x, y, z, new Vector3f());
                    output.add(transformedVertex);
                }
            }
        }
    }

    public static ResourceLocation getTextureLocation(Texture texture) {
        return ResourceLocation.fromNamespaceAndPath(
                "rpt",
                "bbmodel/texture/" + texture.getUuid()
        );
    }

    public static ResourceLocation getTextureLocation(String uuid, @Nullable List<Texture> textures) {
        if (textures == null || textures.isEmpty())  {
            return ResourceLocation.fromNamespaceAndPath(
                    "rpt",
                    "bbmodel/texture/" + uuid
            );
        }

        boolean indexedReference = uuid != null && uuid.startsWith("#");
        String ref = indexedReference ? uuid.substring(1) : uuid;
        if (ref == null) {
            ref = "missing";
        }

        if (indexedReference) {
            try {
                int index = Integer.parseInt(ref);
                if (index >= 0 && index < textures.size()) {
                    return getTextureLocation(textures.get(index));
                }
            } catch (NumberFormatException ignored) {
            }
        }

        for (Texture texture : textures) {
            if (ref.equals(texture.getUuid())) {
                return getTextureLocation(texture);
            }
        }

        try {
            int numericRef = Integer.parseInt(ref);

            if (!indexedReference && numericRef >= 0 && numericRef < textures.size()) {
                return getTextureLocation(textures.get(numericRef));
            }

            for (Texture texture : textures) {
                if (texture.getId() != null && texture.getId() == numericRef) {
                    return getTextureLocation(texture);
                }
            }
        } catch (NumberFormatException ignored) {
        }

        for (Texture texture : textures) {
            if (ref.equals(texture.getName())) {
                return getTextureLocation(texture);
            }
        }

        return ResourceLocation.fromNamespaceAndPath(
                "rpt",
                "bbmodel/texture/" + ref
        );
    }

    private static ResourceLocation getTextureLocation(String reference, @Nullable List<Texture> textures, TextureUtils textureUtils, @Nullable ResourceLocation playerSkin) {
        Texture texture = textureUtils.getTextureByReference(reference);
        if (texture != null) {
            if ("rpt_holder".equals(texture.getName()) && playerSkin != null) return playerSkin;
            return getTextureLocation(texture);
        }
        return getTextureLocation(reference, textures);
    }

    private TextureUtils.AlphaMode resolveAlphaMode(String textureReference, ResourceLocation textureLocation, TextureUtils textureUtils) {
        TextureUtils.AlphaMode cached = TEXTURE_ALPHA_MODES.get(textureLocation);
        if (cached != null) {
            return cached;
        }
        return textureUtils.getAlphaModeByReference(textureReference);
    }

    private static TextureUtils.AlphaMode classifyAlphaMode(NativeImage image) {
        boolean hasZeroAlpha = false;
        boolean hasPartialAlpha = false;

        for (int y = 0; y < image.getHeight() && !hasPartialAlpha; y++) {
            for (int x = 0; x < image.getWidth() && !hasPartialAlpha; x++) {
                int alpha = (image.getPixel(x, y) >>> 24) & 0xFF;
                if (alpha == 0) {
                    hasZeroAlpha = true;
                } else if (alpha < 255) {
                    hasPartialAlpha = true;
                }
            }
        }

        if (hasPartialAlpha) {
            return TextureUtils.AlphaMode.TRANSLUCENT;
        }
        if (hasZeroAlpha) {
            return TextureUtils.AlphaMode.CUTOUT;
        }
        return TextureUtils.AlphaMode.OPAQUE;
    }

    private void renderFace(RenderUtils.RenderableFace face, VertexConsumer consumer, PoseStack poseStack, int packedLight, int packedOverlay) {
        PoseStack.Pose pose = poseStack.last();
        renderFace(face, consumer, pose.pose(), pose.normal(), packedLight, packedOverlay);
    }

    private void renderFace(RenderUtils.RenderableFace face, VertexConsumer consumer, Matrix4f matrix4f, Matrix3f normalMatrix, int packedLight, int packedOverlay) {

        float[] normal = face.getNormal() != null
                ? new float[]{(float)face.getNormal()[0], (float)face.getNormal()[1], (float)face.getNormal()[2]}
                : calculateNormal(face.getVertices());
        float transformedNormalX = normalMatrix.m00() * normal[0] + normalMatrix.m10() * normal[1] + normalMatrix.m20() * normal[2];
        float transformedNormalY = normalMatrix.m01() * normal[0] + normalMatrix.m11() * normal[1] + normalMatrix.m21() * normal[2];
        float transformedNormalZ = normalMatrix.m02() * normal[0] + normalMatrix.m12() * normal[1] + normalMatrix.m22() * normal[2];

        double[][] vertices = face.getVertices();
        double[][] faceVertexUvs = face.getVertexUvs();
        boolean hasVertexUvs = faceVertexUvs != null && faceVertexUvs.length >= vertices.length;
        double[] uvBounds = hasVertexUvs ? null : face.getUv();
        float u1 = uvBounds != null && uvBounds.length > 0 ? (float) uvBounds[0] : 0f;
        float v1 = uvBounds != null && uvBounds.length > 1 ? (float) uvBounds[1] : 0f;
        float u2 = uvBounds != null && uvBounds.length > 2 ? (float) uvBounds[2] : 1f;
        float v2 = uvBounds != null && uvBounds.length > 3 ? (float) uvBounds[3] : 1f;

        for (int i = 0; i < vertices.length; i++) {
            double[] vertex = vertices[i];

            float u;
            float v;
            if (hasVertexUvs) {
                double[] uv = faceVertexUvs[i];
                u = uv != null && uv.length > 0 ? (float) uv[0] : 0f;
                v = uv != null && uv.length > 1 ? (float) uv[1] : 0f;
            } else if (vertices.length == 4) {
                u = (i == 1 || i == 2) ? u2 : u1;
                v = (i == 2 || i == 3) ? v2 : v1;
            } else {
                u = u1;
                v = v1;
            }

            float localX = (float) vertex[0] / 16.0f;
            float localY = (float) vertex[1] / 16.0f;
            float localZ = (float) vertex[2] / 16.0f;
            float transformedX = matrix4f.m00() * localX + matrix4f.m10() * localY + matrix4f.m20() * localZ + matrix4f.m30();
            float transformedY = matrix4f.m01() * localX + matrix4f.m11() * localY + matrix4f.m21() * localZ + matrix4f.m31();
            float transformedZ = matrix4f.m02() * localX + matrix4f.m12() * localY + matrix4f.m22() * localZ + matrix4f.m32();

            consumer.addVertex(transformedX, transformedY, transformedZ)
                    .setColor(255, 255, 255, 255)
                    .setUv(u, v)
                    .setOverlay(packedOverlay)
                    .setLight(packedLight)
                    .setNormal(transformedNormalX, transformedNormalY, transformedNormalZ);
        }
    }

    private float[] calculateNormal(double[][] vertices) {
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

    private TextureRenderData resolveTextureRenderData(String textureReference, List<Texture> textures, TextureUtils textureUtils, Map<String, TextureRenderData> cache, @Nullable ResourceLocation playerSkin) {
        String key = textureReference != null ? textureReference : "__null__";
        TextureRenderData cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        ResourceLocation textureLocation = getTextureLocation(textureReference, textures, textureUtils, playerSkin);
        TextureUtils.AlphaMode alphaMode = resolveAlphaMode(textureReference, textureLocation, textureUtils);
        RenderType renderType = switch (alphaMode) {
            case OPAQUE -> RenderType.entitySolid(textureLocation);
            case CUTOUT -> RenderType.entityCutout(textureLocation);
            case TRANSLUCENT -> RenderType.entityTranslucent(textureLocation);
        };

        TextureRenderData created = new TextureRenderData(textureLocation, alphaMode, renderType);
        cache.put(key, created);
        return created;
    }

    private double computeFaceDepth(RenderUtils.RenderableFace face, Matrix4f poseMatrix, boolean hasCamera, double cameraX, double cameraY, double cameraZ) {
        double[] localCenter = face.getLocalCenter();
        if (localCenter == null || localCenter.length < 3) {
            return 0.0;
        }

        float centerX = (float) localCenter[0] / 16.0f;
        float centerY = (float) localCenter[1] / 16.0f;
        float centerZ = (float) localCenter[2] / 16.0f;
        float transformedX = poseMatrix.m00() * centerX + poseMatrix.m10() * centerY + poseMatrix.m20() * centerZ + poseMatrix.m30();
        float transformedY = poseMatrix.m01() * centerX + poseMatrix.m11() * centerY + poseMatrix.m21() * centerZ + poseMatrix.m31();
        float transformedZ = poseMatrix.m02() * centerX + poseMatrix.m12() * centerY + poseMatrix.m22() * centerZ + poseMatrix.m32();

        if (hasCamera) {
            double dx = transformedX - cameraX;
            double dy = transformedY - cameraY;
            double dz = transformedZ - cameraZ;
            return dx * dx + dy * dy + dz * dz;
        }
        return transformedZ;
    }

    private record TextureRenderData(ResourceLocation location, TextureUtils.AlphaMode alphaMode, RenderType renderType) {}

    private record FaceRenderData(RenderUtils.RenderableFace face, TextureRenderData textureData, Matrix4f poseMatrix, Matrix3f normalMatrix, double depth) {}

}
