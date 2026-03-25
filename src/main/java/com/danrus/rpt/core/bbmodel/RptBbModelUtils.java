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
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
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
import java.util.function.Consumer;

public class RptBbModelUtils implements BbModelRenderer {

    private static final RptBbModelUtils INSTANCE = new RptBbModelUtils();

    private RptBbModelUtils() {}

    public static RptBbModelUtils getInstance() {
        return INSTANCE;
    }

    private static final Logger log = LoggerFactory.getLogger(RptBbModelUtils.class);
    private static final Map<Identifier, TextureUtils.AlphaMode> TEXTURE_ALPHA_MODES = new ConcurrentHashMap<>();
    private final Map<BbModelDocument, com.danrus.rpt.core.bbmodel.baked.BakedModelData> BAKED_MODEL_CACHE = Collections.synchronizedMap(new WeakHashMap<>());
    private final ThreadLocal<HashMap<String, TextureRenderData>> TEXTURE_RENDER_DATA_CACHE = ThreadLocal.withInitial(HashMap::new);

    @Override
    public void renderToBuffer(BbModelDocument model, MultiBufferSource bufferSource, PoseStack poseStack, int packedLight, int packedOverlay) {
        renderToBuffer(model, bufferSource, poseStack, packedLight, packedOverlay, null);
    }

    public void renderToBuffer(BbModelDocument model, MultiBufferSource bufferSource, PoseStack poseStack, int packedLight, int packedOverlay, @Nullable LivingEntity holder) {
        TextureUtils utils = TextureUtils.forDocument(model);
        List<Texture> textures = utils.getAllTextures();

        com.danrus.rpt.core.bbmodel.baked.BakedModelData bakedData = BAKED_MODEL_CACHE.computeIfAbsent(
                model,
                key -> com.danrus.rpt.core.bbmodel.baked.ModelBaker.bakeModel(RenderUtils.forDocument(key).getAllMeshes())
        );

        com.danrus.rpt.core.bbmodel.baked.BakedModelRenderer.render(
                bakedData, bufferSource, poseStack.last(), packedLight, packedOverlay,
                (texRef) -> resolveTextureRenderData(texRef, textures, utils, TEXTURE_RENDER_DATA_CACHE.get(), holder instanceof LocalPlayer player ? player.getSkin().body().texturePath() : null)
        );
    }

    public void renderToBuffer(BbModelDocument model, MultiBufferSource bufferSource, PoseStack poseStack, int packedLight, int packedOverlay, List<AnimationBlendState> activeAnimations, @Nullable LivingEntity holder) {
        if (activeAnimations == null || activeAnimations.isEmpty()) {
            renderToBuffer(model, bufferSource, poseStack, packedLight, packedOverlay, holder);
            return;
        }
        TextureUtils utils = TextureUtils.forDocument(model);
        List<Texture> textures = utils.getAllTextures();
        @Nullable Identifier location = holder instanceof LocalPlayer player ? player.getSkin().body().texturePath() : null;
        
        com.danrus.rpt.core.bbmodel.baked.BakedModelData bakedData = BAKED_MODEL_CACHE.computeIfAbsent(
                model,
                key -> com.danrus.rpt.core.bbmodel.baked.ModelBaker.bakeModel(RenderUtils.forDocument(key).getAllMeshes())
        );
        
        java.util.Map<String, com.danrus.bb4j.api.utils.TransformUtils.Transform> animatedTransforms = 
                com.danrus.bb4j.api.utils.TransformUtils.forDocument(model).getBlendedTransforms(activeAnimations);

        com.danrus.rpt.core.bbmodel.baked.BakedModelRenderer.renderDynamic(
                bakedData, bufferSource, poseStack.last(), packedLight, packedOverlay,
                (texRef) -> resolveTextureRenderData(texRef, textures, utils, TEXTURE_RENDER_DATA_CACHE.get(), location),
                animatedTransforms
        );
    }

    public void renderToBuffer(BbModelDocument model, MultiBufferSource bufferSource, PoseStack poseStack, int packedLight, int packedOverlay, @Nullable String animation, double animationTime, @Nullable LivingEntity holder) {
        if (animation == null || animation.isEmpty()) {
            renderToBuffer(model, bufferSource, poseStack, packedLight, packedOverlay);
            return;
        }
        TextureUtils utils = TextureUtils.forDocument(model);
        List<Texture> textures = utils.getAllTextures();
        @Nullable Identifier location = holder instanceof LocalPlayer player ? player.getSkin().body().texturePath() : null;

        com.danrus.rpt.core.bbmodel.baked.BakedModelData bakedData = BAKED_MODEL_CACHE.computeIfAbsent(
                model,
                key -> com.danrus.rpt.core.bbmodel.baked.ModelBaker.bakeModel(RenderUtils.forDocument(key).getAllMeshes())
        );
        
        com.danrus.bb4j.model.animation.Animation anim = com.danrus.bb4j.api.utils.AnimationUtils.forDocument(model).getAnimationByName(animation);
        java.util.Map<String, com.danrus.bb4j.api.utils.TransformUtils.Transform> animatedTransforms = 
                anim != null ? com.danrus.bb4j.api.utils.TransformUtils.forDocument(model).getAllTransformsAtTime(anim, animationTime) : java.util.Collections.emptyMap();

        com.danrus.rpt.core.bbmodel.baked.BakedModelRenderer.renderDynamic(
                bakedData, bufferSource, poseStack.last(), packedLight, packedOverlay,
                (texRef) -> resolveTextureRenderData(texRef, textures, utils, TEXTURE_RENDER_DATA_CACHE.get(), location),
                animatedTransforms
        );
    }

    public void renderDynamicToBuffer(BbModelDocument model, MultiBufferSource bufferSource, PoseStack.Pose pose, int packedLight, int packedOverlay, List<AnimationBlendState> activeAnimations, @Nullable Identifier playerSkin) {
        if (activeAnimations == null || activeAnimations.isEmpty()) {
            renderStaticToBuffer(model, bufferSource, pose, packedLight, packedOverlay, playerSkin);
            return;
        }
        TextureUtils utils = TextureUtils.forDocument(model);
        List<Texture> textures = utils.getAllTextures();
        
        com.danrus.rpt.core.bbmodel.baked.BakedModelData bakedData = BAKED_MODEL_CACHE.computeIfAbsent(
                model,
                key -> com.danrus.rpt.core.bbmodel.baked.ModelBaker.bakeModel(RenderUtils.forDocument(key).getAllMeshes())
        );
        
        java.util.Map<String, com.danrus.bb4j.api.utils.TransformUtils.Transform> animatedTransforms = 
                com.danrus.bb4j.api.utils.TransformUtils.forDocument(model).getBlendedTransforms(activeAnimations);

        com.danrus.rpt.core.bbmodel.baked.BakedModelRenderer.renderDynamic(
                bakedData, bufferSource, pose, packedLight, packedOverlay,
                (texRef) -> resolveTextureRenderData(texRef, textures, utils, TEXTURE_RENDER_DATA_CACHE.get(), playerSkin),
                animatedTransforms
        );
    }

    public void renderStaticToBuffer(BbModelDocument model, MultiBufferSource bufferSource, PoseStack.Pose pose, int packedLight, int packedOverlay, @Nullable Identifier playerSkin) {
        TextureUtils utils = TextureUtils.forDocument(model);
        List<Texture> textures = utils.getAllTextures();

        com.danrus.rpt.core.bbmodel.baked.BakedModelData bakedData = BAKED_MODEL_CACHE.computeIfAbsent(
                model,
                key -> com.danrus.rpt.core.bbmodel.baked.ModelBaker.bakeModel(RenderUtils.forDocument(key).getAllMeshes())
        );

        com.danrus.rpt.core.bbmodel.baked.BakedModelRenderer.render(
                bakedData, bufferSource, pose, packedLight, packedOverlay,
                (texRef) -> resolveTextureRenderData(texRef, textures, utils, TEXTURE_RENDER_DATA_CACHE.get(), playerSkin)
        );
    }

    public void renderStaticOpaque(BbModelDocument model, MultiBufferSource bufferSource, PoseStack.Pose pose, int packedLight, int packedOverlay, @Nullable Identifier playerSkin, List<com.danrus.rpt.core.bbmodel.baked.BakedModelRenderer.TranslucentQuad> outTranslucent) {
        TextureUtils utils = TextureUtils.forDocument(model);
        List<Texture> textures = utils.getAllTextures();

        com.danrus.rpt.core.bbmodel.baked.BakedModelData bakedData = BAKED_MODEL_CACHE.computeIfAbsent(
                model,
                key -> com.danrus.rpt.core.bbmodel.baked.ModelBaker.bakeModel(RenderUtils.forDocument(key).getAllMeshes())
        );

        com.danrus.rpt.core.bbmodel.baked.BakedModelRenderer.renderOpaque(
                bakedData, bufferSource, pose, packedLight, packedOverlay,
                (texRef) -> resolveTextureRenderData(texRef, textures, utils, TEXTURE_RENDER_DATA_CACHE.get(), playerSkin),
                outTranslucent
        );
    }

    public void renderStaticTranslucent(List<com.danrus.rpt.core.bbmodel.baked.BakedModelRenderer.TranslucentQuad> quads, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        com.danrus.rpt.core.bbmodel.baked.BakedModelRenderer.renderTranslucent(quads, bufferSource, packedLight, packedOverlay);
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
                        Identifier textureLocation = getTextureLocation(texture);
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

    public void getExtentsForGui(BbModelDocument model, PoseStack poseStack,
                                 //? <=1.21.10
                                 //Set<Vector3f>
                                 //? >=1.21.11
                                 Consumer<Vector3fc>
                                 output) {
        getExtentsForGui(model, poseStack, output, null, 0);
    }

    public void getExtentsForGui(BbModelDocument model, PoseStack poseStack,
                                 //? <=1.21.10
                                 //Set<Vector3f>
                                 //? >=1.21.11
                                 Consumer<Vector3fc>
                                 output, String animation, double animationTime) {
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
                    output
                            //? <=1.21.10
                            //.add
                            //? >=1.21.11
                            .accept
                            (transformedVertex);
                }
            }
        }
    }

    public static Identifier getTextureLocation(Texture texture) {
        return Identifier.fromNamespaceAndPath(
                "rpt",
                "bbmodel/texture/" + texture.getUuid()
        );
    }

    public static Identifier getTextureLocation(String uuid, @Nullable List<Texture> textures) {
        if (textures == null || textures.isEmpty())  {
            return Identifier.fromNamespaceAndPath(
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

        return Identifier.fromNamespaceAndPath(
                "rpt",
                "bbmodel/texture/" + ref
        );
    }

    private static Identifier getTextureLocation(String reference, @Nullable List<Texture> textures, TextureUtils textureUtils, @Nullable Identifier playerSkin) {
        Texture texture = textureUtils.getTextureByReference(reference);
        if (texture != null) {
            if ("rpt_holder".equals(texture.getName()) && playerSkin != null) return playerSkin;
            return getTextureLocation(texture);
        }
        return getTextureLocation(reference, textures);
    }

    private TextureUtils.AlphaMode resolveAlphaMode(String textureReference, Identifier textureLocation, TextureUtils textureUtils) {
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

    private TextureRenderData resolveTextureRenderData(String textureReference, List<Texture> textures, TextureUtils textureUtils, Map<String, TextureRenderData> cache, @Nullable Identifier playerSkin) {
        String key = textureReference != null ? textureReference : "__null__";
        TextureRenderData cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        Identifier textureLocation = getTextureLocation(textureReference, textures, textureUtils, playerSkin);
        TextureUtils.AlphaMode alphaMode = resolveAlphaMode(textureReference, textureLocation, textureUtils);
        RenderType renderType = switch (alphaMode) {
            //? <=1.21.10 {
            /*case OPAQUE -> RenderType.entitySolid(textureLocation);
            case CUTOUT -> RenderType.entityCutout(textureLocation);
            case TRANSLUCENT -> RenderType.entityTranslucent(textureLocation);
            *///?} else {
            case OPAQUE -> net.minecraft.client.renderer.rendertype.RenderTypes.entitySolid(textureLocation);
            case CUTOUT -> net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(textureLocation);
            case TRANSLUCENT -> net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(textureLocation);
            //?}
        };

        TextureRenderData created = new TextureRenderData(textureLocation, alphaMode, renderType);
        cache.put(key, created);
        return created;
    }

    public record TextureRenderData(Identifier location, TextureUtils.AlphaMode alphaMode, RenderType renderType) {}

}
