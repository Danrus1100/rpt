package com.danrus.rpt.core.arm;

import com.danrus.rpt.core.expression.NumericExpression;
import com.danrus.rpt.core.item.RptField;
import com.danrus.rpt.core.item.RptVariables;
import com.danrus.rpt.duck.CustomArmTransformHolder;
import com.ezylang.evalex.Expression;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public record ArmTransform(NumericExpression x, NumericExpression y, NumericExpression z, boolean bob, boolean swing, boolean attack, boolean armorStands, String ofVanilla, RptVariables variables) {

    public ArmTransform(NumericExpression x, NumericExpression y, NumericExpression z, boolean bob, boolean swing, boolean attack, boolean armorStands, String ofVanilla) {
        this(x, y, z, bob, swing, attack, armorStands, ofVanilla, RptVariables.EMPTY);
    }

    public ArmTransform(ArmTransform other, RptField field) {
        this(other.x, other.y, other.z, other.bob, other.swing, other.attack, other.armorStands(), other.ofVanilla, field.variables());
    }

    public static ArmTransform EMPTY = new ArmTransform(NumericExpression.ZERO, NumericExpression.ZERO, NumericExpression.ZERO, true, true, true, false, "");

    private static final Map<String, Expression> EXPR_CACHE = new ConcurrentHashMap<>();

    public boolean isEmpty() {
        return x == NumericExpression.ZERO &&
                y == NumericExpression.ZERO &&
                z == NumericExpression.ZERO &&
                bob && swing && ofVanilla.isEmpty();
    }

    public static ArmTransform fromVanilla(String name) {
        return new ArmTransform(NumericExpression.ZERO, NumericExpression.ZERO, NumericExpression.ZERO, true, true, true, false, name);
    }

    public void rotateModelPart(ModelPart arm, ModelPart head, boolean isRightArm, HumanoidRenderState state) {
        if (!swing) arm.resetPose();

        DeltaTracker deltaTracker = Minecraft.getInstance().getDeltaTracker();
        float delta = state.isFullyFrozen ? 1.0f : deltaTracker.getGameTimeDeltaPartialTick(true);

        long time = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.getGameTime() : 0;

        Map<String, Double> vars = new HashMap<>();

        Map<String, Double> varsGame = Map.of(
                "time", (double) time,
                "delta", (double) delta,
                "hx", Math.toDegrees(head.xRot),
                "hy", Math.toDegrees(head.yRot),
                "hz", Math.toDegrees(head.zRot),
                "swing", (double) state.attackTime,
                "swingArm", switch (state.attackArm) {
                    case RIGHT -> 1.0;
                    case LEFT -> -1.0;
                },
                "useArm", switch (state.useItemHand) {
                    case MAIN_HAND -> 1.0;
                    case OFF_HAND -> -1.0;
                }
        );

        vars.putAll(varsGame);
        vars.putAll(variables.numbers());

        if (state instanceof PlayerRenderState playerRenderState) {
            vars.putAll(Map.of(
                    //? < 1.21.10 {
                    "useTick", (double) playerRenderState.useItemRemainingTicks,
                    //?} else {
                    /*"useTick", (double) playerRenderState.ticksUsingItem,
                     *///?}
                    "holdArm", isRightArm ? 1.0 : -1.0
            ));
        }

        arm.xRot += (float) Math.toRadians(evaluate(x, vars));
        arm.yRot += (float) Math.toRadians(evaluate(y, vars));
        arm.zRot += (float) Math.toRadians(evaluate(z, vars));
    }

    private static float evaluate(NumericExpression val, Map<String, Double> vars) {
        String exprStr = val.expression();
        if (exprStr.isEmpty()) return 0f;

        Expression exp = EXPR_CACHE.computeIfAbsent(exprStr, Expression::new);
        vars.forEach(exp::with);

        try {
            return exp.evaluate().getNumberValue().floatValue();
        } catch (Exception e) {
            return 0f;
        }
    }

    public static void resetState(CustomArmTransformHolder holder){
        resetState(holder, true, true);
    }

    public static void resetState(CustomArmTransformHolder holder, boolean rightArm){
        resetState(holder, rightArm, !rightArm);
    }

    public static void resetState(CustomArmTransformHolder holder, boolean rightArm, boolean leftArm) {
        if (rightArm) holder.rpt$setRightArmTransform(EMPTY);
        if (leftArm) holder.rpt$setLeftArmTransform(EMPTY);
    }

    @Nullable
    public HumanoidModel.ArmPose getVanillaIfPresent() {
        return switch (ofVanilla) {
            case "empty" -> HumanoidModel.ArmPose.EMPTY;
            case "item"  -> HumanoidModel.ArmPose.ITEM;
            case "block" -> HumanoidModel.ArmPose.BLOCK;
            case "bow_and_arrow" -> HumanoidModel.ArmPose.BOW_AND_ARROW;
            case "throw_trident" ->
                    //? <= 1.21.10
                    HumanoidModel.ArmPose.THROW_SPEAR;
                    //? >= 1.21.11
                    //HumanoidModel.ArmPose.THROW_TRIDENT;
            case "crossbow_charge" -> HumanoidModel.ArmPose.CROSSBOW_CHARGE;
            case "crossbow_hold" -> HumanoidModel.ArmPose.CROSSBOW_HOLD;
            case "spyglass" -> HumanoidModel.ArmPose.SPYGLASS;
            case "toot_horn" -> HumanoidModel.ArmPose.TOOT_HORN;
            case "brush" -> HumanoidModel.ArmPose.BRUSH;
            //? >= 1.21.11
            //case "spear" -> HumanoidModel.ArmPose.SPEAR;
            default -> null;
        };
    }

    private static final Codec<ArmTransform> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NumericExpression.CODEC.optionalFieldOf("x", NumericExpression.ZERO).forGetter(ArmTransform::x),
            NumericExpression.CODEC.optionalFieldOf("y", NumericExpression.ZERO).forGetter(ArmTransform::y),
            NumericExpression.CODEC.optionalFieldOf("z", NumericExpression.ZERO).forGetter(ArmTransform::z),
            Codec.BOOL.optionalFieldOf("bob", true).forGetter(ArmTransform::bob),
            Codec.BOOL.optionalFieldOf("swing", true).forGetter(ArmTransform::swing),
            Codec.BOOL.optionalFieldOf("attack", true).forGetter(ArmTransform::attack),
            Codec.BOOL.optionalFieldOf("enable_armorstands_custom_defrmations_secret", false).forGetter(ArmTransform::armorStands),
            Codec.STRING.optionalFieldOf("type", "").forGetter(ArmTransform::ofVanilla)
    ).apply(instance, ArmTransform::new));

    public static final Codec<ArmTransform> CODEC = Codec.either(Codec.STRING, FULL_CODEC).xmap(
            either -> either.map(ArmTransform::fromVanilla, obj -> obj),
            obj -> {
                if (
                    obj.x().expression().isEmpty() &&
                    obj.y().expression().isEmpty() &&
                    obj.z().expression().isEmpty() &&
                    obj.bob() && obj.swing() && obj.attack()
                ) {
                    return Either.left(obj.ofVanilla());
                }
                return Either.right(obj);
            }
    );

}

