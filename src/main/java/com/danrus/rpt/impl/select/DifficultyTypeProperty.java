package com.danrus.rpt.impl.select;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DifficultyTypeProperty implements SelectItemModelProperty<String> {
    public static final Type<? extends SelectItemModelProperty<String>, String> TYPE = Type.create(MapCodec.unit(new DifficultyTypeProperty()), Codec.STRING);

    private static final String HARDCORE = "hardcore";
    public static final String NONE = "none";

    @Override
    public @Nullable String get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
        if (level == null) return NONE;
        if (level.getLevelData().isHardcore()) return HARDCORE;
        return level.getLevelData().getDifficulty()
                //? <26.1
                .getKey();
                //? >=26.1
                //.getSerializedName();
    }

    @Override
    public @NotNull Codec<String> valueCodec() {
        return Codec.STRING;
    }

    @Override
    public @NotNull Type<? extends SelectItemModelProperty<String>, String> type() {
        return TYPE;
    }
}
