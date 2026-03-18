package com.danrus.rpt.impl.special;

import com.danrus.bb4j.model.BbModelDocument;
import com.danrus.rpt.Rpt;
import com.danrus.rpt.core.bbmodel.BbModelDynamicState;
import com.danrus.rpt.core.bbmodel.RptBbModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Set;

public record BbModelSpecialRenderer(BbModelDocument model) implements SpecialModelRenderer<BbModelDynamicState> {
    @Override
    public void render(@Nullable BbModelDynamicState patterns, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, boolean hasFoilType) {
        if (patterns == null) return;
        RptBbModel.renderToBuffer(model, bufferSource, poseStack); // TODO: apply BbModelDynamicState
    }

    @Override
    public void getExtents(Set<Vector3f> output) {
        PoseStack poseStack = new PoseStack();
        RptBbModel.getExtentsForGui(model, poseStack, output);
    }

    @Override
    public @Nullable BbModelDynamicState extractArgument(ItemStack stack) {
        ResourceLocation location = stack.get(DataComponents.ITEM_MODEL);
        return Rpt.getBbmodelsManager().getDynamicState(location);
    }

    public record Unbaked(ResourceLocation location) implements SpecialModelRenderer.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(unbakedInstance -> unbakedInstance.group(
                ResourceLocation.CODEC.fieldOf("model").forGetter(Unbaked::location)
        ).apply(unbakedInstance, Unbaked::new));

        @Override
        public @NotNull SpecialModelRenderer<?> bake(EntityModelSet modelSet) {
            BbModelDocument document = Rpt.getBbmodelsManager().getModel(location);
            if (document == null) throw new IllegalStateException("unable to find bbmodel " + location);
            return new BbModelSpecialRenderer(document);
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
