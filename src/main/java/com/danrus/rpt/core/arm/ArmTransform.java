package com.danrus.rpt.core.arm;

import com.danrus.rpt.core.expression.NumberOrString;
import com.danrus.rpt.duck.CustomArmTransformHolder;
import com.ezylang.evalex.Expression;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public record ArmTransform(NumberOrString x, NumberOrString y, NumberOrString z, boolean bob, boolean swing, boolean attack, String ofVanilla) {

    public static ArmTransform EMPTY = new ArmTransform(NumberOrString.ZERO, NumberOrString.ZERO, NumberOrString.ZERO, true, true, true, "");

    private static final Map<String, Expression> EXPR_CACHE = new ConcurrentHashMap<>();

    public boolean isEmpty() {
        return x == NumberOrString.ZERO &&
                y == NumberOrString.ZERO &&
                z == NumberOrString.ZERO &&
                bob && swing && ofVanilla.isEmpty();
    }

    public static ArmTransform fromVanilla(String name) {
        return new ArmTransform(NumberOrString.ZERO, NumberOrString.ZERO, NumberOrString.ZERO, true, true, true, name);
    }

    public void rotateModelPart(ModelPart arm, ModelPart head, boolean isRightArm, PlayerRenderState state) {
        if (!swing) arm.resetPose();

        long time = Minecraft.getInstance().level.getGameTime();
        Map<String, Double> vars = Map.of(
                "time", (double) time,
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
                },
                //? < 1.21.10 {
                "useTick", (double) state.useItemRemainingTicks,
                //?} else {
                /*"useTick", (double) state.ticksUsingItem,
                *///?}
                "holdArm", isRightArm ? 1.0 : -1.0
        );

        arm.xRot += (float) Math.toRadians(evaluate(x, vars));
        arm.yRot += (float) Math.toRadians(evaluate(y, vars));
        arm.zRot += (float) Math.toRadians(evaluate(z, vars));
    }

    private static float evaluate(NumberOrString val, Map<String, Double> vars) {
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
            NumberOrString.CODEC.optionalFieldOf("x", NumberOrString.ZERO).forGetter(ArmTransform::x),
            NumberOrString.CODEC.optionalFieldOf("y", NumberOrString.ZERO).forGetter(ArmTransform::y),
            NumberOrString.CODEC.optionalFieldOf("z", NumberOrString.ZERO).forGetter(ArmTransform::z),
            Codec.BOOL.optionalFieldOf("bob", true).forGetter(ArmTransform::bob),
            Codec.BOOL.optionalFieldOf("swing", true).forGetter(ArmTransform::swing),
            Codec.BOOL.optionalFieldOf("attack", true).forGetter(ArmTransform::attack),
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

