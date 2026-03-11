package com.danrus.rpt.impl.model;

import com.danrus.rpf.api.RpfItemModel;
import com.danrus.rpf.api.TestsResultCollector;
import com.danrus.rpf.api.codec.RpfModelsCodecsExtends;
import com.danrus.rpf.core.item.ModelUpdateContext;
import com.danrus.rpt.core.NumberOrString;
import com.danrus.rpt.core.OwnerHolder;
import com.danrus.rpt.core.item.RptItemParams;
import com.danrus.rpt.core.item.RptItemVariables;
import com.danrus.rpt.duck.RptItemParamsHolder;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
//? >=1.21.11 {
/*import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributes;
*///? }
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ExpressionToExpressionsModel extends AbstractRpfItemModel {

    private final String main;
    private final List<BakedExpressionCase> modelExpressions;
    private final ItemModel fallback;
    private final boolean delegate;
    private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();

    public ExpressionToExpressionsModel(String main, List<BakedExpressionCase> modelExpressions, ItemModel fallback, boolean delegate) {
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
        selectModelToUpdate(vars, level, owner.get() == null ? null : owner.get()
                //? >=1.21.10
                //.asLivingEntity()
                , seed).update(
                renderState, stack, itemModelResolver, displayContext, level, owner.get(), seed
        );
    }

    private ItemModel selectModelToUpdate(RptItemVariables vars, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        double mainEval = evaluate(this.main, vars, level, entity, seed);

        for (BakedExpressionCase entry : modelExpressions) {
            int req = 0;
            for (NumberOrString nos : entry.when) {
                String exprStr = nos.expression().trim();

                if (exprStr.startsWith(">") || exprStr.startsWith("<") || exprStr.startsWith("=") || exprStr.startsWith("!")) {
                    if (evaluateCondition(exprStr, mainEval, vars, level, entity, seed)) {
                        if (entry.requireAll) req++;
                        else return entry.model;
                    }
                } else {
                    if (mainEval == evaluate(exprStr, vars, level, entity, seed)) {
                        return entry.model;
                    }
                }

                if (entry.requireAll && req == entry.when.size()) {
                    return entry.model;
                }
            }
        }
        return fallback;
    }

    private boolean evaluateCondition(String condition, double mainValue, RptItemVariables vars, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        String fullExprStr = "_val " + condition;

        Expression expr = expressionCache.computeIfAbsent(fullExprStr, Expression::new);

        expr.with("_val", mainValue);
        gameVariables(level, entity, seed).forEach(expr::with);
        vars.numbers().forEach(expr::with);

        try {
            return expr.evaluate().getBooleanValue();
        } catch (Exception e) {
            return false;
        }
    }

    private double evaluate(String expressionStr, RptItemVariables vars, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        Expression expr = expressionCache.computeIfAbsent(expressionStr, Expression::new);

        gameVariables(level, entity, seed).forEach(expr::with);
        vars.numbers().forEach(expr::with);

        try {
            EvaluationValue result = expr.evaluate();
            if (result.isBooleanValue()) {
                return result.getBooleanValue() ? 1.0 : 0.0;
            }
            return result.getNumberValue().doubleValue();
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static Map<String, Double> gameVariables(@Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        Map<String, Double> vars = new HashMap<>();

        vars.put("gameTime", (double) (level != null ? level.getGameTime() : 0));
        vars.put("dayTime", (double) (level != null ? level.getLevelData().getDayTime() : 0));
        vars.put("seed", (double) seed);

        vars.put("holderX", 0.0);
        vars.put("holderY", 0.0);
        vars.put("holderZ", 0.0);
        vars.put("yaw", 0.0);
        vars.put("pitch", 0.0);
        vars.put("health", 20.0);
        vars.put("maxHealth", 20.0);
        vars.put("motionY", 0.0);
        vars.put("age", 0.0);
        vars.put("lightLevel", 0.0);

        vars.put("food", 20.0);
        vars.put("saturation", 5.0);
        vars.put("experienceLevel", 0.0);
        vars.put("armor", 0.0);

        vars.put("lightSky", 0.0);
        vars.put("lightBlock", 0.0);
        vars.put("lightTotal", 0.0);
        vars.put("biomeTemp", 0.0);
        vars.put("sunAngle", 0.0);
        vars.put("moonPhase", 0.0);


        if (entity != null) {
            Vec3 pos = entity.position();
            vars.put("holderX", pos.x);
            vars.put("holderY", pos.y);
            vars.put("holderZ", pos.z);
            vars.put("yaw", (double) entity.getYRot());
            vars.put("pitch", (double) entity.getXRot());
            vars.put("health", (double) entity.getHealth());
            vars.put("maxHealth", (double) entity.getMaxHealth());
            vars.put("fallDistance", (double) entity.fallDistance);
            vars.put("motionY", entity.getDeltaMovement().y);

            if (entity instanceof AgeableMob mob) {
                vars.put("age", (double) mob.getAge());
            }

            if (entity instanceof Player player) {
                vars.put("food", (double) player.getFoodData().getFoodLevel());
                vars.put("saturation", (double) player.getFoodData().getSaturationLevel());
                vars.put("experienceLevel", (double) player.experienceLevel);
                vars.put("armor", (double) player.getArmorValue());
            }

            BlockPos entityPos = entity.blockPosition();
            if (level != null) {
                int sky = level.getBrightness(LightLayer.SKY, entityPos);
                int block = level.getBrightness(LightLayer.BLOCK, entityPos);

                vars.put("lightSky", (double) sky);
                vars.put("lightBlock", (double) block);
                vars.put("lightTotal", (double) Math.max(sky, block));

                var biome = level.getBiome(entityPos).value();
                vars.put("biomeTemp", (double) biome.getBaseTemperature());

                //? <=1.21.10 {
                vars.put("moonPhase", (double) level.getMoonPhase());
                vars.put("sunAngle", (double) level.getSunAngle(1.0f));
                //?} else {
                /*vars.put("moonPhase", (double) level.environmentAttributes().getDimensionValue(EnvironmentAttributes.MOON_PHASE).index());
                vars.put("sunAngle", level.environmentAttributes().getDimensionValue(EnvironmentAttributes.SUN_ANGLE).doubleValue());
                *///?}
            }
        }

        return vars;
    }

    public static record Unbaked(String main, List<ExpressionCase> expressions, boolean delegate, Optional<ItemModel.Unbaked> fallback) implements ItemModel.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.fieldOf("value").forGetter(Unbaked::main),
                ExpressionCase.CODEC.listOf().fieldOf("cases").forGetter(Unbaked::expressions),
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
            List<BakedExpressionCase> modelMap = new ArrayList<>();
            for (ExpressionCase expression : expressions) {
                modelMap.add(new BakedExpressionCase(expression.when, expression.model.bake(context), expression.requireAll));
            }
            ItemModel fallbackModel = fallback.map(unbaked -> unbaked.bake(context)).orElseGet(context::missingItemModel);
            return new ExpressionToExpressionsModel(main, modelMap, fallbackModel, delegate);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            fallback.ifPresent(m -> m.resolveDependencies(resolver));
            for (ExpressionCase expression : expressions) {
                expression.model.resolveDependencies(resolver);
            }
        }
    }

    public static record ExpressionCase(List<NumberOrString> when, ItemModel.Unbaked model, boolean requireAll) {
        public static final Codec<ExpressionCase> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                NumberOrString.CODEC.listOf().fieldOf("when").forGetter(ExpressionCase::when),
                ItemModels.CODEC.fieldOf("model").forGetter(ExpressionCase::model),
                Codec.BOOL.optionalFieldOf("all", false).forGetter(ExpressionCase::requireAll)
        ).apply(instance, ExpressionCase::new));
    }

    public static record BakedExpressionCase(List<NumberOrString> when , ItemModel model, boolean requireAll){}
}
