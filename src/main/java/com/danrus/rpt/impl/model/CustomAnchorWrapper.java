package com.danrus.rpt.impl.model;

import com.danrus.rpf.api.RpfItemModel;
import com.danrus.rpf.api.TestsResultCollector;
import com.danrus.rpf.api.codec.RpfModelsCodecsExtends;
import com.danrus.rpf.core.item.ModelUpdateContext;
import com.danrus.rpt.core.anchor.AnchorType;
import com.danrus.rpt.duck.RptItemRenderState;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;

public class CustomAnchorWrapper extends AbstractRpfItemModel {

    private final ItemModel model;
    @Nullable
    private final AnchorType anchorType;

    public CustomAnchorWrapper(ItemModel model, AnchorType anchorType) {
        this.model = model;
        this.anchorType = anchorType;
    }

    @Override
    public boolean rpf$doDelegate(ModelUpdateContext context, ItemStack stack, ItemOwner owner, @Nullable ItemModel prev, TestsResultCollector collector) {
        return ((RpfItemModel)model).rpf$doDelegate(context, stack, owner, this, collector);
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, ItemOwner owner, int seed) {
        ((RptItemRenderState)renderState).rpt$setAnchorType(anchorType);
        model.update(renderState, stack, itemModelResolver, displayContext, level, owner, seed);
    }

    public record Unbaked(ItemModel.Unbaked model, AnchorType anchorType) implements ItemModel.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                ItemModels.CODEC.fieldOf("model").forGetter(Unbaked::model),
                AnchorType.CODEC.fieldOf("anchor").forGetter(Unbaked::anchorType)
        ).apply(i, Unbaked::new));

        public static final Identifier ID = Identifier.fromNamespaceAndPath("rpt", "anchor");

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return RpfModelsCodecsExtends.getInstance().wrap(ID, MAP_CODEC);
        }

        @Override
        public ItemModel bake(BakingContext context, Matrix4fc transformation) {
            return new CustomAnchorWrapper(model.bake(context, transformation), anchorType);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            model.resolveDependencies(resolver);
        }
    }
}
