package com.danrus.rpt.impl.special;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.rpt.Rpt;
import com.danrus.rpt.core.bbmodel.BbModelRenderer;
import com.danrus.rpt.core.bbmodel.DynamicSpecialModel;
import com.danrus.rpt.core.bbmodel.fsm.FsmInstance;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Set;

public record BbModelSpecialRenderer(ResourceLocation location, BbModelDocument model) implements DynamicSpecialModel<FsmInstance> {
    @Override
    public void render(@Nullable FsmInstance fsmInstance, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, boolean hasFoilType) {
        if (fsmInstance == null) {
            BbModelRenderer.get().renderToBuffer(model, bufferSource, poseStack, packedLight, packedOverlay);
            return;
        }
        BbModelRenderer.get().renderToBuffer(model, bufferSource, poseStack, packedLight, packedOverlay, fsmInstance.getBlendStates(), fsmInstance.getCapturedEntity());
    }

    @Override
    public void getExtents(Set<Vector3f> output) {}

    @Override
    public @Nullable FsmInstance extractArgument(ItemStack stack) { return null; }

    @Override
    public FsmInstance extractArgument(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        FsmInstance instance = Rpt.getBbmodelsManager().getDynamicState(location, seed, entity, stack, displayContext);
        if (instance == null) return null;
        instance.tick(
                Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks() / 20.0f,
                displayContext,
                level, entity, seed, model
        );
        return instance;
    }

    @Override
    public void getExtends(FsmInstance patterns, ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, Set<Vector3f> output) {
        PoseStack poseStack = new PoseStack();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.scale(16f, 16f, 16f); // for cases if animations move item out of extends box
        } else {
            poseStack.scale(-1.0F, -1.0F, 1.0F);
        }
        renderState.setAnimated();
        BbModelRenderer.get().getExtentsForGui(model, poseStack, output);
    }

    public record Unbaked(ResourceLocation location) implements SpecialModelRenderer.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(unbakedInstance -> unbakedInstance.group(
                ResourceLocation.CODEC.fieldOf("model").forGetter(Unbaked::location)
        ).apply(unbakedInstance, Unbaked::new));

        @Override
        public @NotNull SpecialModelRenderer<?> bake(EntityModelSet modelSet) {
            BbModelDocument document = Rpt.getBbmodelsManager().getModel(location);
            if (document == null) throw new IllegalStateException("unable to find bbmodel " + location);
            return new BbModelSpecialRenderer(location, document);
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
