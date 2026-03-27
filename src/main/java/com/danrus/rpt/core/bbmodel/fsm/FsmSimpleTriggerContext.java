package com.danrus.rpt.core.bbmodel.fsm;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public record FsmSimpleTriggerContext(String trigger, @Nullable LivingEntity entity) {
}
