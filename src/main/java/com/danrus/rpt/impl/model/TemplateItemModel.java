package com.danrus.rpt.impl.model;

import com.danrus.rpf.api.RpfItemModel;
import com.danrus.rpf.api.codec.RpfModelsCodecsExtends;
import com.danrus.rpf.logging.ModelTestsResultCollector;
import com.danrus.rpt.Rpt;
import com.danrus.rpt.core.OwnerHolder;
import com.danrus.rpt.core.item.RptItemParams;
import com.danrus.rpt.core.template.RptTemplate;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TemplateItemModel extends AbstractRpfItemModel {

    private final RptTemplate template;

    public TemplateItemModel(RptTemplate template) {
        this.template = template;
    }

    @Override
    public boolean rpf$doDelegate(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, OwnerHolder owner, @Nullable ItemModel prev, int seed, Identifier itemModelId, String packName, ModelTestsResultCollector collector) {
        collector.touchInfo(getClass().getSimpleName() + ": delegating to template model: " + template.getClass().getSimpleName(), packName, itemModelId);
        return ((RpfItemModel) template.model()).rpf$doDelegate(renderState, stack, itemModelResolver, displayContext, level, owner.get(), prev, seed, itemModelId, packName, collector);
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, OwnerHolder owner,int seed) {
        RptItemParams merged = RptItemParams.merge(template.params(), RptItemParams.fromItemStack(stack));
        RptItemParams.putToItemStack(stack, merged);
        template.model().update(renderState, stack, itemModelResolver, displayContext, level, owner.get(), seed);
    }

    public static record Unbaked(Identifier templateId) implements ItemModel.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("template").forGetter(Unbaked::templateId)
        ).apply(instance, Unbaked::new));
        public static final Identifier ID = Identifier.fromNamespaceAndPath("rpt", "template");

        @Override
        @NotNull
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return RpfModelsCodecsExtends.getInstance().wrap(ID, MAP_CODEC);
        }

        @Override
        public @NotNull ItemModel bake(BakingContext context) {
            RptTemplate template = Rpt.getTemplatesManager().getTemplate(templateId);
            return new TemplateItemModel(template);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(templateId);
        }
    }
}
