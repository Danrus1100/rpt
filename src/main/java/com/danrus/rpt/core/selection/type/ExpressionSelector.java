package com.danrus.rpt.core.selection.type;

import com.danrus.rpt.core.expression.ExpressionsCase;
import com.danrus.rpt.core.expression.GameExpressionsHelper;
import com.danrus.rpt.core.expression.NumericExpression;
import com.danrus.rpt.core.selection.NestedSelector;
import com.danrus.rpt.core.selection.NestedSelectors;
import com.danrus.rpt.core.selection.SelectResult;
import com.danrus.rpt.core.selection.SelectionContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record ExpressionSelector<T>(String main, List<ExpressionsCase<NestedSelector<T>>> cases, NestedSelector<T> fallback) implements NestedSelector<T> {
    @Override
    public void resolveSelect(ItemStack stack, @Nullable LivingEntity entity, SelectionContext<T> context) {
        Level level = entity != null ? entity.level() : null;
        ClientLevel clientLevel = level != null && level.isClientSide() ? (ClientLevel) level : null;

        SelectResult<NestedSelector<T>> result =  GameExpressionsHelper.selectResultFromCases(cases, main, Map.of(), clientLevel, entity, entity != null ? entity.getId() : 0, fallback);
        // ignore fallback if it ignored
        if  (!result.isFallback() || context.allowFallbacks()) {
            result.result().resolveSelect(stack, entity, context);
        }
    }

    public static record Unbaked<T>(NumericExpression main, List<ExpressionsCase<NestedSelector.Unbaked<T>>> cases, Optional<NestedSelector.Unbaked<T>> fallback) implements NestedSelector.Unbaked<T> {

        @SuppressWarnings("unchecked")
        public static synchronized <T> MapCodec<Unbaked<T>> codec(Codec<T> valueCodec) {
            return RecordCodecBuilder.mapCodec(unbakedInstance -> unbakedInstance.group(
                    NumericExpression.CODEC.fieldOf("value").forGetter(Unbaked::main),
                    ExpressionsCase.codec(NestedSelectors.codec(valueCodec), "child").listOf().fieldOf("cases").forGetter(Unbaked::cases),
                    NestedSelectors.codec(valueCodec).optionalFieldOf("fallback").forGetter(Unbaked::fallback)
            ).apply(unbakedInstance, Unbaked::new));
        }

        @Override
        public BakeResult<T> bakeResult() {
            List<ExpressionsCase<NestedSelector<T>>> baked = new ArrayList<>(cases.size());
            for (ExpressionsCase<NestedSelector.Unbaked<T>> entry : cases) {
                baked.add(new BakedExpressionsCase(entry.when(), entry.value().bakeResult().selector(), entry.requireAll()));
            }
            NestedSelector<T> bakedFallback = fallback.isPresent() ? fallback.get().bakeResult().selector() : EmptySelector.instance();
            return new BakeResult<>(new ExpressionSelector<>(main.expression(), baked, bakedFallback), fallback.isPresent());
        }

        @Override
        public MapCodec<? extends NestedSelector.Unbaked<T>> type(Codec<T> valueCodec) {
            return codec(valueCodec);
        }
    }

    public static class BakedExpressionsCase<T> extends ExpressionsCase<NestedSelector<T>> {
        protected BakedExpressionsCase(List<NumericExpression> when, NestedSelector<T> value, boolean requireAll) {
            super(when, value, requireAll);
        }

        @Override
        protected Codec<NestedSelector<T>> createValueCodec() {
            throw new UnsupportedOperationException("NestedSelector is not serializable!");
        }
    }
}
