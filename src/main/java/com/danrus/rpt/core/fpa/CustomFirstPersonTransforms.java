package com.danrus.rpt.core.fpa;

import com.danrus.rpt.core.item.transfrom.ItemTransformDefinition;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;

public record CustomFirstPersonTransforms(
        Optional<ItemTransformDefinition> main,
        Optional<ItemTransformDefinition> offhand,
        boolean attack,
        boolean use
) {

    public static final CustomFirstPersonTransforms DEFAULT = CustomFirstPersonTransforms.single(ItemTransformDefinition.NONE, true, true);

    public CustomFirstPersonTransforms(ItemTransformDefinition main, ItemTransformDefinition offhand, boolean attack, boolean use) {
        this(Optional.of(main), Optional.of(offhand), attack, use);
    }

    public static CustomFirstPersonTransforms single(ItemTransformDefinition def, boolean attack, boolean use) {
        return new CustomFirstPersonTransforms(def, def, attack, use);
    }

    private static final Codec<CustomFirstPersonTransforms> FULL_CODEC = RecordCodecBuilder.create(i -> i.group(
            ItemTransformDefinition.CODEC.optionalFieldOf("main").forGetter(CustomFirstPersonTransforms::main),
            ItemTransformDefinition.CODEC.optionalFieldOf("offhand").forGetter(CustomFirstPersonTransforms::offhand),
            Codec.BOOL.optionalFieldOf("attack", true).forGetter(CustomFirstPersonTransforms::attack),
            Codec.BOOL.optionalFieldOf("use", true).forGetter(CustomFirstPersonTransforms::use)
    ).apply(i, CustomFirstPersonTransforms::new));

    public static final Codec<CustomFirstPersonTransforms> CODEC = Codec.either(FULL_CODEC, ItemTransformDefinition.CODEC).xmap(
            either -> either.map(
                    fpt -> fpt,
                    def -> CustomFirstPersonTransforms.single(def, false, false)
            ),
            fpt -> {
                if (fpt.main.isPresent() && fpt.main.equals(fpt.offhand)) {
                    return Either.right(fpt.main.orElse(ItemTransformDefinition.NONE));
                }
                if (fpt.main.isEmpty() && fpt.offhand.isEmpty()) {
                    return Either.right(ItemTransformDefinition.NONE);
                }
                return Either.left(fpt);
            }
    );

    public boolean isSingle() {
        if (isBothExplicitlyEmpty()) {
            return false;
        }
        return main.equals(offhand) || main.isPresent() != offhand.isPresent();
    }

    public boolean isBothExplicitlyEmpty() {
        return main.isEmpty() && offhand.isEmpty();
    }

    public boolean isDefault() {
        return getMain().isDefault() && getOffhand().isDefault() && attack && use;
    }

    public ItemTransformDefinition getMain() {
        return main.orElse(ItemTransformDefinition.NONE);
    }

    public ItemTransformDefinition getOffhand() {
        return offhand.orElse(getMain());
    }

    public ItemTransformDefinition get(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> getMain();
            case OFF_HAND -> getOffhand();
        };
    }

    public boolean applyToPose(AbstractClientPlayer player, InteractionHand hand, boolean leftHand, float progress, PoseStack poseStack) {
        ItemTransformDefinition transformDefinition = get(hand);
        ItemTransform transform = transformDefinition.evaluateWithGame(
                Map.of(
                        "drawProgress", (double) progress,
                        "rightHand", leftHand ? 0.0 : 1.0
                ),
                player.level().isClientSide() ? (ClientLevel) player.level() : null,
                player,
                player.getId()
            );
        transform.apply(false, poseStack.last());
        return true;
    }
}