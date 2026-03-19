package com.danrus.rpt.core.fpa;

import com.danrus.rpt.core.item.transfrom.ItemTransformDefinition;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.InteractionHand;

import java.util.Optional;

public record CustomFirstPersonAnimations(
        Optional<ItemTransformDefinition> main,
        Optional<ItemTransformDefinition> offhand,
        boolean attack,
        boolean use
) {
    private static final ItemTransformDefinition DEFAULT = ItemTransformDefinition.NONE;

    public CustomFirstPersonAnimations(ItemTransformDefinition main, ItemTransformDefinition offhand, boolean attack, boolean use) {
        this(Optional.of(main), Optional.of(offhand), attack, use);
    }

    public static CustomFirstPersonAnimations single(ItemTransformDefinition def, boolean attack, boolean use) {
        return new CustomFirstPersonAnimations(def, def, attack, use);
    }

    private static final Codec<CustomFirstPersonAnimations> FULL_CODEC = RecordCodecBuilder.create(i -> i.group(
            ItemTransformDefinition.CODEC.optionalFieldOf("main").forGetter(CustomFirstPersonAnimations::main),
            ItemTransformDefinition.CODEC.optionalFieldOf("offhand").forGetter(CustomFirstPersonAnimations::offhand),
            Codec.BOOL.optionalFieldOf("attack", true).forGetter(CustomFirstPersonAnimations::attack),
            Codec.BOOL.optionalFieldOf("use", true).forGetter(CustomFirstPersonAnimations::use)
    ).apply(i, CustomFirstPersonAnimations::new));

    public static final Codec<CustomFirstPersonAnimations> CODEC = Codec.either(ItemTransformDefinition.CODEC, FULL_CODEC).xmap(
            either -> either.map(
                    def -> CustomFirstPersonAnimations.single(def, false, false),
                    fpt -> fpt
            ),
            fpt -> {
                if (fpt.main.isPresent() && fpt.main.equals(fpt.offhand)) {
                    return Either.left(fpt.main.orElse(ItemTransformDefinition.NONE));
                }
                if (fpt.main.isEmpty() && fpt.offhand.isEmpty()) {
                    return Either.left(DEFAULT);
                }
                return Either.right(fpt);
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

    public ItemTransformDefinition getMain() {
        return main.orElse(DEFAULT);
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
}