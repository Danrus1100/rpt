package com.danrus.rpt.impl.select;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BiomeLocationProperty implements SelectItemModelProperty<Identifier> {

    public static final Type<? extends SelectItemModelProperty<Identifier>, Identifier> TYPE = Type.create(MapCodec.unit(new BiomeLocationProperty()), Identifier.CODEC);

    @Override
    public @Nullable Identifier get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
        if (entity == null || level == null) return null;

        try {
            return level
                    .registryAccess()
                    .lookupOrThrow(Registries.BIOME)
                    .getKey(
                            level.getBiome(
                                    new BlockPos(
                                            (int) entity.position().x,
                                            (int) entity.position().y,
                                            (int) entity.position().z)
                            ).value()
                    );
        } catch (Throwable throwable) {
            return null;
        }
    }

    @Override
    public @NotNull Codec<Identifier> valueCodec() {
        return Identifier.CODEC;
    }

    @Override
    public @NotNull Type<? extends SelectItemModelProperty<Identifier>, Identifier> type() {
        return TYPE;
    }
}
