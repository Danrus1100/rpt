package com.danrus.rpt.impl.model;

import com.danrus.rpf.api.RpfItemModel;
import com.danrus.rpf.api.TestsResultCollector;
import com.danrus.rpf.api.codec.RpfModelsCodecsExtends;
import com.danrus.rpf.core.item.ModelUpdateContext;
import com.danrus.rpt.core.OwnerHolder;
import com.danrus.rpt.core.RptUnbakedModel;
import com.danrus.rpt.duck.RptBakingContext;
import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class VariableItemModelWrapper extends AbstractRpfItemModel  {

    private final ItemModel model;

    public VariableItemModelWrapper(ItemModel model) {
        this.model = model;
    }

    @Override
    boolean rpf$doDelegate(ModelUpdateContext context, ItemStack stack, OwnerHolder owner, @Nullable ItemModel prev, TestsResultCollector collector) {
        return RpfItemModel.class.cast(model).rpf$doDelegate(context, stack, owner.get(), prev, collector);
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, OwnerHolder owner, int seed) {
        model.update(renderState, stack, itemModelResolver, displayContext, level, owner.get(), seed);
    }

    public static record Unbaked(String variable, List<ItemTintSource> tints
                                 , Optional<Transformation> transformation
    ) implements RptUnbakedModel {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                Codec.STRING.fieldOf("variable")
                        .forGetter(Unbaked::variable),
                ItemTintSources.CODEC.listOf()
                        .optionalFieldOf("tints", List.of())
                        .forGetter(Unbaked::tints)

                , Transformation.EXTENDED_CODEC
                        .optionalFieldOf("transformation")
                        .forGetter(Unbaked::transformation)
        ).apply(instance, Unbaked::new));

        public static final Identifier ID = Identifier.fromNamespaceAndPath("rpt", "variable");

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return RpfModelsCodecsExtends.getInstance().wrap(ID, MAP_CODEC);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {

        }

        @Override
        public ItemModel bake(BakingContext context, Baker baker) {
            Identifier model = RptBakingContext.class.cast(context).rpt$getField().variables().models().get(variable);
            if (model == null) {
                throw new IllegalStateException("Can't find model from variable: " + variable);
            }
            CuboidItemModelWrapper.Unbaked unbaked = new CuboidItemModelWrapper.Unbaked(model
                    , transformation
                    , tints);
            return new VariableItemModelWrapper(baker.bake(context, unbaked));
        }
    }
}
