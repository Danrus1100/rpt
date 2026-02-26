package com.danrus.rpt.core.arm;

import com.danrus.rpt.duck.CustomArmTransformHolder;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public record ArmTransform(float x, float y, float z, boolean bob, boolean swing, boolean toHead, boolean isEmpty, String ofVanilla) {

    public static ArmTransform EMPTY = new ArmTransform(0, 0 , 0, true, true, false, true, "");

    public ArmTransform(float x, float y, float z, boolean bob, boolean swing, boolean toHead, String ofVanilla) {
        this(x, y, z, bob, swing, toHead, false, ofVanilla);
    }

    public static ArmTransform fromVanilla(String name) {
        return new ArmTransform(0, 0, 0, true, true, false, false, name);
    }


    public void rotateModelPart(ModelPart arm, ModelPart head, boolean isRightArm) {
        if (!swing) arm.resetPose();

        float baseX = (float) Math.toRadians(x);
        float baseY = (float) Math.toRadians(y);
        float baseZ = (float) Math.toRadians(z);

        if (toHead) {
            float cosZ = (float) Math.cos(baseZ);
            float sinZ = (float) Math.sin(baseZ);

            float cosY = (float) Math.cos(baseY);

            // vertical
            arm.xRot += baseX + (head.xRot * cosZ);
            arm.yRot += baseY - (head.xRot * sinZ);

            // horizontal
            arm.yRot += (head.yRot * cosZ) * cosY;
            arm.xRot += (head.yRot * sinZ) * cosY;

            arm.zRot = baseZ;
        } else {
            arm.xRot += baseX;
            arm.yRot += baseY;
            arm.zRot += baseZ;
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
                    //HumanoidModel.ArmPose.THROW_SPEAR;
                    //? >= 1.21.11
                    HumanoidModel.ArmPose.THROW_TRIDENT;
            case "crossbow_charge" -> HumanoidModel.ArmPose.CROSSBOW_CHARGE;
            case "crossbow_hold" -> HumanoidModel.ArmPose.CROSSBOW_HOLD;
            case "spyglass" -> HumanoidModel.ArmPose.SPYGLASS;
            case "toot_horn" -> HumanoidModel.ArmPose.TOOT_HORN;
            case "brush" -> HumanoidModel.ArmPose.BRUSH;
            //? >= 1.21.11
            case "spear" -> HumanoidModel.ArmPose.SPEAR;
            default -> null;
        };
    }

    private static final Codec<ArmTransform> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("x", 0f).forGetter(ArmTransform::x),
            Codec.FLOAT.optionalFieldOf("y", 0f).forGetter(ArmTransform::y),
            Codec.FLOAT.optionalFieldOf("z", 0f).forGetter(ArmTransform::z),
            Codec.BOOL.optionalFieldOf("bob", true).forGetter(ArmTransform::bob),
            Codec.BOOL.optionalFieldOf("swing", true).forGetter(ArmTransform::swing),
                Codec.BOOL.optionalFieldOf("to_head", false).forGetter(ArmTransform::toHead),
            Codec.STRING.optionalFieldOf("type", "").forGetter(ArmTransform::ofVanilla)
    ).apply(instance, ArmTransform::new));

    public static final Codec<ArmTransform> CODEC = Codec.either(Codec.STRING, FULL_CODEC).xmap(
            either -> either.map(ArmTransform::fromVanilla, obj -> obj),
            obj -> {
                // Если это просто ссылка на ванильную позу (координаты 0), пишем строку
                if (obj.x() == 0 && obj.y() == 0 && obj.z() == 0 && obj.bob() && obj.swing() && !obj.toHead()) {
                    return Either.left(obj.ofVanilla());
                }
                return Either.right(obj);
            }
    );
}

