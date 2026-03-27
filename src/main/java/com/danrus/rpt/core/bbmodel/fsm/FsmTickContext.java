package com.danrus.rpt.core.bbmodel.fsm;

import com.danrus.bb4j.model.BbModelDocument;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record FsmTickContext(ItemStack stack, @Nullable LivingEntity entity, @Nullable ClientLevel level, int seed, ItemDisplayContext context, BbModelDocument model) {}
