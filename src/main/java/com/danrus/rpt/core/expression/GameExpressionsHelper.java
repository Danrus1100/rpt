package com.danrus.rpt.core.expression;

import com.danrus.rpt.core.selection.SelectResult;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

//? >=1.21.11 {
/*import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributes;
*///? }

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class GameExpressionsHelper implements IdentifiableResourceReloadListener {

    private static final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();
    private static final List<String> alreadyLogged = new ArrayList<>();

    public static final String RESERVED_VARIABLE_NAME = "_val";
    private static final Logger log = LoggerFactory.getLogger(GameExpressionsHelper.class);

    public static <T> T selectValueFromCases(List<? extends ExpressionsCase<T>> cases, String expression, Map<String, Double> additionalVars, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, T fallback) {
        return selectResultFromCases(cases, expression, additionalVars, level, entity, seed, fallback).result();
    }

    public static <T> SelectResult<T> selectResultFromCases(List<? extends ExpressionsCase<T>> cases, String expression, Map<String, Double> additionalVars, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, T fallback) {
        double mainEval = evaluate(expression, additionalVars, level, entity, seed);

        for (ExpressionsCase<T> entry : cases) {
            int req = 0;
            for (NumericExpression nos : entry.when()) {
                String exprStr = nos.expression().trim();

                if (exprStr.startsWith(">") || exprStr.startsWith("<") || exprStr.startsWith("=") || exprStr.startsWith("!")) {
                    if (evaluateCondition(exprStr, mainEval, additionalVars, level, entity, seed)) {
                        if (entry.requireAll()) req++;
                        else return SelectResult.ok(entry.value());
                    }
                } else {
                    if (mainEval == evaluate(exprStr, additionalVars, level, entity, seed)) {
                        return SelectResult.ok(entry.value());
                    }
                }

                if (entry.requireAll() && req == entry.when().size()) {
                    return SelectResult.ok(entry.value());
                }
            }
        }
        return SelectResult.fallback(fallback);
    }

    private static void logError(String expression, Exception e) {
        if (alreadyLogged.contains(expression)) return;
        log.error("Failed to evaluate expression {}", expression, e);
        alreadyLogged.add(expression);
    }

    public static boolean evaluateCondition(String condition, double mainValue, Map<String, Double> additionalVars, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        String fullExprStr = RESERVED_VARIABLE_NAME + " " + condition;

        Expression expr = expressionCache.computeIfAbsent(fullExprStr, Expression::new);

        expr.with(RESERVED_VARIABLE_NAME, mainValue);
        generateGameVariables(level, entity, seed).forEach(expr::with);
        additionalVars.forEach(expr::with);

        try {
            return expr.evaluate().getBooleanValue();
        } catch (Exception e) {
            logError(condition, e);
            return false;
        }
    }

    public static void onReload() {
        expressionCache.clear();
        alreadyLogged.clear();
    }

    public static double evaluate(String expressionStr, Map<String, Double> additionalVars, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        Expression expr = expressionCache.computeIfAbsent(expressionStr, Expression::new);

        generateGameVariables(level, entity, seed).forEach(expr::with);
        additionalVars.forEach(expr::with);

        try {
            EvaluationValue result = expr.evaluate();
            if (result.isBooleanValue()) {
                return result.getBooleanValue() ? 1.0 : 0.0;
            }
            return result.getNumberValue().doubleValue();
        } catch (Exception e) {
            logError(expressionStr, e);
            return 0.0;
        }
    }

    private static Map<String, Double> generateGameVariables(@Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        Map<String, Double> vars = new HashMap<>();

        DeltaTracker deltaTracker = Minecraft.getInstance().getDeltaTracker();
        float delta = (entity != null && level != null && level.tickRateManager().isEntityFrozen(entity))
                ? 1.0F
                : deltaTracker.getGameTimeDeltaPartialTick(true);

        vars.put("gameTime", (double) (level != null ? level.getGameTime() : 0));
        vars.put("dayTime", (double) (level != null ? level.getLevelData().getDayTime() : 0));
        vars.put("delta", (double) delta);
        vars.put("seed", (double) seed);

        vars.put("holderX", 0.0);
        vars.put("holderY", 0.0);
        vars.put("holderZ", 0.0);
        vars.put("yaw", 0.0);
        vars.put("pitch", 0.0);
        vars.put("health", 20.0);
        vars.put("maxHealth", 20.0);
        vars.put("motionY", 0.0);

        vars.put("motionX", 0.0);
        vars.put("motionZ", 0.0);
        vars.put("speed", 0.0);
        vars.put("horizontalSpeed", 0.0);
        vars.put("onGround", 0.0);
        vars.put("isSprinting", 0.0);
        vars.put("isCrouching", 0.0);
        vars.put("isInWater", 0.0);
        vars.put("isInLava", 0.0);
        vars.put("isSwimming", 0.0);
        vars.put("isFallFlying", 0.0);

        vars.put("isAlive", 0.0);
        vars.put("isOnFire", 0.0);
        vars.put("isInvisible", 0.0);
        vars.put("hurtTime", 0.0);
        vars.put("deathTime", 0.0);
        vars.put("invulnerableTime", 0.0);

        vars.put("age", 0.0);
        vars.put("lightLevel", 0.0);

        vars.put("food", 20.0);
        vars.put("saturation", 5.0);
        vars.put("experienceLevel", 0.0);
        vars.put("armor", 0.0);

        vars.put("xpProgress", 0.0);
        vars.put("totalXp", 0.0);
        vars.put("air", 0.0);
        vars.put("maxAir", 0.0);
        vars.put("attackCooldown", 0.0);
        vars.put("sleeping", 0.0);
        vars.put("attackProgress", 0.0);

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
            vars.put("motionX", entity.getDeltaMovement().x);
            vars.put("motionZ", entity.getDeltaMovement().z);
            vars.put("speed", entity.getDeltaMovement().length());
            vars.put("horizontalSpeed", Math.sqrt(
                    entity.getDeltaMovement().x * entity.getDeltaMovement().x +
                            entity.getDeltaMovement().z * entity.getDeltaMovement().z
            ));
            vars.put("onGround", entity.onGround() ? 1.0 : 0.0);
            vars.put("isSprinting", entity.isSprinting() ? 1.0 : 0.0);
            vars.put("isCrouching", entity.isCrouching() ? 1.0 : 0.0);
            vars.put("isInWater", entity.isInWater() ? 1.0 : 0.0);
            vars.put("isInLava", entity.isInLava() ? 1.0 : 0.0);
            vars.put("isSwimming", entity.isSwimming() ? 1.0 : 0.0);
            vars.put("isFallFlying", entity.isFallFlying() ? 1.0 : 0.0);

            vars.put("isAlive", entity.isAlive() ? 1.0 : 0.0);
            vars.put("isOnFire", entity.isOnFire() ? 1.0 : 0.0);
            vars.put("isInvisible", entity.isInvisible() ? 1.0 : 0.0);
            vars.put("hurtTime", (double) entity.hurtTime);
            vars.put("deathTime", (double) entity.deathTime);
            vars.put("invulnerableTime", (double) entity.invulnerableTime);

            if (entity instanceof AgeableMob mob) {
                vars.put("age", (double) mob.getAge());
            }

            if (entity instanceof Player player) {
                vars.put("food", (double) player.getFoodData().getFoodLevel());
                vars.put("saturation", (double) player.getFoodData().getSaturationLevel());
                vars.put("experienceLevel", (double) player.experienceLevel);
                vars.put("armor", (double) player.getArmorValue());

                vars.put("xpProgress", (double) player.experienceProgress);
                vars.put("totalXp", (double) player.totalExperience);
                vars.put("air", (double) player.getAirSupply());
                vars.put("maxAir", (double) player.getMaxAirSupply());
                vars.put("attackCooldown", (double) player.getAttackStrengthScale(delta));
                vars.put("sleeping", player.isSleeping() ? 1.0 : 0.0);

                // 1.3.0 start
                vars.put("attackProgress", (double) player.getAttackAnim(delta));
                float currUsageTime = player.getUseItemRemainingTicks() - delta + 1.0F;
                @Nullable
                ItemStack itemStack = player.getUseItem();
                vars.put("usageProgress", itemStack.isEmpty() ? 0.0 : currUsageTime / itemStack.getUseDuration(player));
                // 1.3.0 end
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
                vars.put("sunAngle", (double) level.getSunAngle(delta));
                //?} else {
                /*vars.put("moonPhase", (double) level.environmentAttributes().getDimensionValue(EnvironmentAttributes.MOON_PHASE).index());
                vars.put("sunAngle", level.environmentAttributes().getDimensionValue(EnvironmentAttributes.SUN_ANGLE).doubleValue());
                *///?}
            }
        }

        return vars;
    }

    @Override
    public @NotNull CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, Executor backgroundExecutor, Executor gameExecutor) {
        onReload();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath("rpt", "expressions_helper");
    }
}
