package com.danrus.rpt.impl.special;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.rpt.Rpt;
import com.danrus.rpt.core.bbmodel.BbBoneAttachmentUtils;
import com.danrus.rpt.core.bbmodel.BbModelRenderer;
import com.danrus.rpt.core.bbmodel.BbModelStateIdentity;
import com.danrus.rpt.core.bbmodel.DynamicSpecialModel;
import com.danrus.rpt.core.bbmodel.fsm.FsmInstance;
import com.danrus.rpt.core.bbmodel.fsm.FsmTickContext;
import com.danrus.rpt.core.bbmodel.nodes.BbModelsSubmitsCollector;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Optional;
import java.util.function.Consumer;

public record BbModelSpecialRenderer(Identifier location, BbModelDocument model, @Nullable ModelLink link, ItemStackRenderState itemStackRenderState) implements DynamicSpecialModel<FsmInstance> {

    public BbModelSpecialRenderer(Identifier location, BbModelDocument model, @Nullable ModelLink link) {
        this(location, model, link, new ItemStackRenderState());
    }

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
        if (link != null) {
            if (link.uuid() != null) {
                BbBoneAttachmentUtils.withBoneByUuidAndGroupElementCenter(model, poseStack, link().uuid(), fsmInstance, () -> itemStackRenderState.submit(
                        poseStack, nodeCollector, packedLight, packedOverlay, outlineColor
                ));
            }
        }
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
        return Rpt.getFsmManager().queue(
                BbModelStateIdentity.of(location, seed),
                new FsmTickContext(stack, entity, level, seed, displayContext, model)
        );
    }

    @Override
    public void updateAndGetExtends(FsmInstance patterns, ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed,
                                    //? <=1.21.10
                                    // Set<Vector3f>
                                    //? >=1.21.11
                                    Consumer<Vector3fc>
                                   output) {
        PoseStack poseStack = new PoseStack();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        renderState.setAnimated();
        if (displayContext == ItemDisplayContext.GUI) {
            // Gui bounds fix
            float guiBoundsPadding = 4F;
            output.accept(new Vector3f(-guiBoundsPadding, -guiBoundsPadding, -guiBoundsPadding));
            output.accept(new Vector3f(guiBoundsPadding, guiBoundsPadding, guiBoundsPadding));
        } else {
            BbModelRenderer.get().getExtentsForGui(model, poseStack, output);
        }
        if (link != null) {
            this.itemStackRenderState.clear();
            link.model().update(this.itemStackRenderState, stack, itemModelResolver, displayContext, level, entity, seed);
        }
    }

    private static @Nullable String findGroupUuidByName(BbModelDocument model, @Nullable String groupName) {
        if (groupName == null || groupName.isBlank() || model.getGroups() == null || model.getGroups().isEmpty()) {
            return null;
        }

        for (BbModelDocument.Group group : model.getGroups()) {
            if (group != null && groupName.equals(group.getName())) {
                return group.getUuid();
            }
        }
        return null;
    }

    public record Unbaked(Identifier location, Optional<UnbakedModelLink> modelLink) implements DynamicSpecialModel.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(unbakedInstance -> unbakedInstance.group(
                Identifier.CODEC.fieldOf("model").forGetter(Unbaked::location),
                UnbakedModelLink.CODEC.optionalFieldOf("attachment").forGetter(Unbaked::modelLink)
        ).apply(unbakedInstance, Unbaked::new));

        public @NotNull SpecialModelRenderer<?> bake(EntityModelSet modelSet) {
            return null; // NO-OP
        }

        public @Nullable SpecialModelRenderer<?> bake(BakingContext context) {
            return bake(context.entityModelSet());
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public @Nullable SpecialModelRenderer<?> bake(ItemModel.BakingContext context) {
            BbModelDocument document = Rpt.getBbmodelsManager().getModel(location);
            if (document == null) throw new IllegalStateException("unable to find bbmodel " + location);
            ModelLink bakedLink = modelLink.map(unbakedModelLink -> unbakedModelLink.bake(context, document)).orElse(null);
            return new BbModelSpecialRenderer(location, document, bakedLink);
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            modelLink.ifPresent(modelLink1 -> modelLink1.model.resolveDependencies(resolver));
        }
    }

    public record UnbakedModelLink(String group, ItemModel.Unbaked model) {

        public static final Codec<UnbakedModelLink> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("group").forGetter(UnbakedModelLink::group),
                ItemModels.CODEC.fieldOf("model").forGetter(UnbakedModelLink::model)
        ).apply(i, UnbakedModelLink::new));

        public ModelLink bake(ItemModel.BakingContext context, BbModelDocument modelDocument) {
            return new ModelLink(findGroupUuidByName(modelDocument, group), model.bake(context));
        }


    }

    public record ModelLink(String uuid, ItemModel model) {}
}
