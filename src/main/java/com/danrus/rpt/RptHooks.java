package com.danrus.rpt;

import com.danrus.rpf.core.item.ModelUpdateContext;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class RptHooks {
    public static void preTick() {

    }

    public static void postTick() {
    }

    public static void preRender(GameRenderer renderer, DeltaTracker deltaTracker) {
        Rpt.getFsmManager().tick(deltaTracker, Minecraft.getInstance().level);
    }

    public static void postRender(GameRenderer renderer, DeltaTracker deltaTracker) {

    }

    public static void preItemResolve(
            ModelUpdateContext context,
            ItemStack stack,
            @Nullable LivingEntity entity
    ) {

    }

    public static void postItemResolve(
            ModelUpdateContext context,
            ItemStack stack,
            @Nullable LivingEntity entity
    ) {

    }
}
