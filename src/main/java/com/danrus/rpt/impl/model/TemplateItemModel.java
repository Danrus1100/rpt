package com.danrus.rpt.impl.model;

import com.danrus.rpf.api.RpfItemModel;
import com.danrus.rpf.api.TestsResultCollector;
import com.danrus.rpf.api.codec.RpfModelsCodecsExtends;
import com.danrus.rpf.core.item.ModelUpdateContext;
import com.danrus.rpt.Rpt;
import com.danrus.rpt.core.OwnerHolder;
import com.danrus.rpt.core.item.RptField;
import com.danrus.rpt.core.template.RptTemplate;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.ResourceLocation;
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
    boolean rpf$doDelegate(ModelUpdateContext context, ItemStack stack, OwnerHolder owner, @Nullable ItemModel prev, TestsResultCollector collector) {
        collector.info(getClass().getSimpleName() + ": delegating to template model: " + template.getClass().getSimpleName());
        return ((RpfItemModel) template.model()).rpf$doDelegate(context, stack, owner.get(), this, collector);
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, OwnerHolder owner,int seed) {
        RptField merged = RptField.merge(template.params(), RptField.fromItemStack(stack));
        RptField.putToItemStack(stack, merged);
        template.model().update(renderState, stack, itemModelResolver, displayContext, level, owner.get(), seed);
    }

    public static record Unbaked(ResourceLocation templateId) implements ItemModel.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("template").forGetter(Unbaked::templateId)
        ).apply(instance, Unbaked::new));
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("rpt", "template");

        @Override
        @NotNull
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return RpfModelsCodecsExtends.getInstance().wrap(ID, MAP_CODEC);
        }

        @Override
        public @NotNull ItemModel bake(BakingContext context) {
            RptTemplate template = Rpt.getTemplatesManager().getTemplate(context, templateId);
            if (template == null) {
                throw new IllegalStateException("can't find template " + templateId);
            }
            return new TemplateItemModel(template);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
//            resolver.markDependency(templateId);
        }
    }
}
