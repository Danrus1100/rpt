package com.danrus.rpt.impl.model;

import com.danrus.rpf.api.RpfItemModel;
import com.danrus.rpf.api.TestsResultCollector;
import com.danrus.rpf.api.codec.RpfModelsCodecsExtends;
import com.danrus.rpf.core.item.ModelUpdateContext;
import com.danrus.rpt.core.expression.ExpressionsCase;
import com.danrus.rpt.core.expression.GameExpressionsHelper;
import com.danrus.rpt.core.expression.NumberOrString;
import com.danrus.rpt.core.OwnerHolder;
import com.danrus.rpt.core.item.RptItemParams;
import com.danrus.rpt.core.item.RptItemVariables;
import com.danrus.rpt.duck.RptItemParamsHolder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ExpressionToExpressionsModel extends AbstractRpfItemModel {

    private final String main;
    private final List<BakedModelExpressionsCase> modelExpressions;
    private final ItemModel fallback;
    private final boolean delegate;

    public ExpressionToExpressionsModel(String main, List<BakedModelExpressionsCase> modelExpressions, ItemModel fallback, boolean delegate) {
        this.main = main;
        this.modelExpressions = modelExpressions;
        this.fallback = fallback;
        this.delegate = delegate;
        ((RpfItemModel) this.fallback).rpf$markAsFallback();
    }

    @Override
    boolean rpf$doDelegate(ModelUpdateContext context, ItemStack stack, OwnerHolder owner, @Nullable ItemModel prev, TestsResultCollector collector) {
        if (!delegate) {
            collector.hit(ExpressionToExpressionsModel.class, "force cancel delegation");
            return false;
        }
        RptItemVariables vars = RptItemParamsHolder.class.cast(stack).rpt$getParams().orElse(RptItemParams.EMPTY).variables();
        return  ((RpfItemModel)selectModelToUpdate(vars, context.level(), owner.get() == null ? null : owner.get()
                //? >=1.21.10
                 //.asLivingEntity()
                , context.seed()
        )).rpf$doDelegate(context, stack, owner.get(), prev, collector);
    }

    @Override
    void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, OwnerHolder owner, int seed) {
        RptItemVariables vars = RptItemParamsHolder.class.cast(stack).rpt$getParams().orElse(RptItemParams.EMPTY).variables();
        selectModelToUpdate(vars, level, owner.get() == null ? null : owner.asLivingEntity()
                , seed).update(
                renderState, stack, itemModelResolver, displayContext, level, owner.get(), seed
        );
    }

    private ItemModel selectModelToUpdate(RptItemVariables vars, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        return GameExpressionsHelper.selectValueFromCases(modelExpressions, main, vars.numbers(), level, entity, seed, fallback);
    }

    public static record Unbaked(String main, List<ExpressionsCase<ItemModel.Unbaked>> expressions, boolean delegate, Optional<ItemModel.Unbaked> fallback) implements ItemModel.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.fieldOf("value").forGetter(Unbaked::main),
                ExpressionsCase.codec(ItemModels.CODEC, "model").listOf().fieldOf("cases").forGetter(Unbaked::expressions),
                Codec.BOOL.optionalFieldOf("delegate", true).forGetter(Unbaked::delegate),
                ItemModels.CODEC.optionalFieldOf("fallback").forGetter(Unbaked::fallback)
        ).apply(i, Unbaked::new));

        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("rpt", "expression");

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return RpfModelsCodecsExtends.getInstance().wrap(ID, MAP_CODEC);
        }

        @Override
        public ItemModel bake(BakingContext context) {
            List<BakedModelExpressionsCase> modelMap = new ArrayList<>();
            for (ExpressionsCase<ItemModel.Unbaked> expression : expressions) {
                modelMap.add(new BakedModelExpressionsCase(expression.when(), expression.value().bake(context), expression.requireAll()));
            }
            ItemModel fallbackModel = fallback.map(unbaked -> unbaked.bake(context)).orElseGet(context::missingItemModel);
            return new ExpressionToExpressionsModel(main, modelMap, fallbackModel, delegate);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            fallback.ifPresent(m -> m.resolveDependencies(resolver));
            for (ExpressionsCase<ItemModel.Unbaked> expression : expressions) {
                expression.value().resolveDependencies(resolver);
            }
        }
    }

    public static class BakedModelExpressionsCase extends ExpressionsCase<ItemModel>{
        protected BakedModelExpressionsCase(List<NumberOrString> when, ItemModel value, boolean requireAll) {
            super(when, value, requireAll);
        }

        @Override
        protected Codec<ItemModel> createValueCodec() {
            throw new UnsupportedOperationException("ItemModel is not serializable!");
        }
    }
}
