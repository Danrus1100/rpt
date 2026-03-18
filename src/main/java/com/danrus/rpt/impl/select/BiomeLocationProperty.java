package com.danrus.rpt.impl.select;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BiomeLocationProperty implements SelectItemModelProperty<ResourceLocation> {

    public static final Type<? extends SelectItemModelProperty<ResourceLocation>, ResourceLocation> TYPE = Type.create(MapCodec.unit(new BiomeLocationProperty()), ResourceLocation.CODEC);

    @Override
    public @Nullable ResourceLocation get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
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
    public @NotNull Codec<ResourceLocation> valueCodec() {
        return ResourceLocation.CODEC;
    }

    @Override
    public @NotNull Type<? extends SelectItemModelProperty<ResourceLocation>, ResourceLocation> type() {
        return TYPE;
    }
}
