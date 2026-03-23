package com.danrus.rpt.core.anchor;

import com.mojang.serialization.Codec;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum AnchorType implements StringRepresentable {
    LEFT_HAND("right_hand"),
    RIGHT_HAND("left_hand"),
    BODY("body"),
    HEAD("head"),
    LEFT_LEG("left_leg"),
    RIGHT_LEG("right_leg");

    public static final Codec<AnchorType> CODEC = StringRepresentable.fromEnum(AnchorType::values);

    private final String name;

    AnchorType(String name) {
        this.name = name;
    }

    public ModelPart getPart(HumanoidModel<?> model) {
        return switch (this) {
            case LEFT_HAND -> model.leftArm;
            case RIGHT_HAND -> model.rightArm;
            case BODY -> model.body;
            case HEAD -> model.head;
            case LEFT_LEG -> model.leftLeg;
            case RIGHT_LEG -> model.rightLeg;
        };
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
