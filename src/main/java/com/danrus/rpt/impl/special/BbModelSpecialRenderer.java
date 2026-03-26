package com.danrus.rpt.impl.special;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.rpt.Rpt;
import com.danrus.rpt.core.bbmodel.BbModelRenderer;
import com.danrus.rpt.core.bbmodel.DynamicSpecialModel;
import com.danrus.rpt.core.bbmodel.fsm.FsmInstance;
import com.danrus.rpt.core.bbmodel.nodes.BbModelsSubmitsCollector;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Set;
import java.util.function.Consumer;

public record BbModelSpecialRenderer(Identifier location, BbModelDocument model) implements DynamicSpecialModel<FsmInstance> {
    public void render(@Nullable FsmInstance fsmInstance, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, boolean hasFoilType) {
        if (fsmInstance == null) {
            BbModelRenderer.get().renderToBuffer(model, bufferSource, poseStack, packedLight, packedOverlay);
            return;
        }
        poseStack.pushPose();
        BbModelRenderer.get().renderToBuffer(model, bufferSource, poseStack, packedLight, packedOverlay, fsmInstance.getBlendStates(), fsmInstance.getCapturedEntity());
        poseStack.popPose();
    }

    public void submit(@Nullable FsmInstance fsmInstance, ItemDisplayContext displayContext, PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector nodeCollector, int packedLight, int packedOverlay, boolean hasFoil, int outlineColor) {
        poseStack.pushPose();
//        nodeCollector.submitModel();
        BbModelsSubmitsCollector.from(nodeCollector).rpt$submitBbModel(poseStack, model, fsmInstance, fsmInstance != null && fsmInstance.getCapturedEntity() instanceof LocalPlayer player ? player.getSkin().body().texturePath() : null, packedLight, packedOverlay);
        poseStack.popPose();
    }

    @Override
    public void getExtents(
            //? <=1.21.10
            //Set<Vector3f>
            //? >=1.21.10
            Consumer<Vector3fc>
            output) {}

    @Override
    public @Nullable FsmInstance extractArgument(ItemStack stack) { return null; }

    @Override
    public FsmInstance extractArgument(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        FsmInstance instance = Rpt.getBbmodelsManager().getDynamicState(location, seed, entity, stack, displayContext);
        if (instance == null) return null;
        instance.tick(
                Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks() / 20.0f,
                displayContext,
                stack, level, entity, seed, model
        );
        return instance;
    }

    @Override
    public void getExtends(FsmInstance patterns, ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed,
                           //? <=1.21.10
                           // Set<Vector3f>
                           //? >=1.21.11
                           Consumer<Vector3fc>
                                   output) {
        PoseStack poseStack = new PoseStack();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.scale(-1f, -1f, 16f); // for cases if animations move item out of extends box
        } else {
            poseStack.scale(-1.0F, -1.0F, 1.0F);
        }
        renderState.setAnimated();
        BbModelRenderer.get().getExtentsForGui(model, poseStack, output);
    }

    public record Unbaked(Identifier location) implements SpecialModelRenderer.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(unbakedInstance -> unbakedInstance.group(
                Identifier.CODEC.fieldOf("model").forGetter(Unbaked::location)
        ).apply(unbakedInstance, Unbaked::new));

        public @NotNull SpecialModelRenderer<?> bake(EntityModelSet modelSet) {
            BbModelDocument document = Rpt.getBbmodelsManager().getModel(location);
            if (document == null) throw new IllegalStateException("unable to find bbmodel " + location);
            return new BbModelSpecialRenderer(location, document);
        }

        public @Nullable SpecialModelRenderer<?> bake(BakingContext context) {
            return bake(context.entityModelSet());
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
