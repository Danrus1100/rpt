package com.danrus.rpt.impl.model;

import com.danrus.rpf.api.RpfItemModel;
import com.danrus.rpf.api.codec.RpfModelsCodecsExtends;
import com.danrus.rpf.logging.ModelTestsResultCollector;
import com.danrus.rpt.duck.RptBakingContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VariableBlockModelWrapper extends AbstractRpfItemModel  {

    private final ItemModel model;

    public VariableBlockModelWrapper(ItemModel model) {
        this.model = model;
    }

    @Override
    public boolean rpf$doDelegate(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity owner, @Nullable ItemModel prev, int seed, ResourceLocation itemModelId, String packName, ModelTestsResultCollector collector) {
        return RpfItemModel.class.cast(model).rpf$doDelegate(renderState, stack, itemModelResolver, displayContext, level, owner, prev, seed, itemModelId, packName, collector);
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        model.update(renderState, stack, itemModelResolver, displayContext, level, entity, seed);
    }

    public static record Unbaked(String variable, List<ItemTintSource> tints) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                Codec.STRING.fieldOf("variable")
                        .forGetter(Unbaked::variable),
                ItemTintSources.CODEC.listOf()
                        .optionalFieldOf("tints", List.of())
                        .forGetter(Unbaked::tints)
        ).apply(instance, Unbaked::new));

        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("rpt", "variable");

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return RpfModelsCodecsExtends.getInstance().wrap(ID, MAP_CODEC);
        }

        @Override
        public ItemModel bake(BakingContext context) {
            ResourceLocation model = RptBakingContext.class.cast(context).rpt$getParams().variables().models().get(variable);
            if (model == null) {
                throw new IllegalStateException("Can't find model from variable: " + variable);
            }
            BlockModelWrapper.Unbaked unbaked = new BlockModelWrapper.Unbaked(model, tints);
            return new VariableBlockModelWrapper(unbaked.bake(context));
        }

        @Override
        public void resolveDependencies(Resolver resolver) {

        }
    }
}
