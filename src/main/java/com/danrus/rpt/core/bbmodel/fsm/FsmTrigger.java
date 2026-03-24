package com.danrus.rpt.core.bbmodel.fsm;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public interface FsmTrigger {
    /**
     * @param activeTriggers Set of string IDs that were triggered this tick
     * @param customVariables Variables passed to the FSM instance
     * @param level Client level
     * @param entity The entity holding the model
     * @param seed Random seed
     * @return true if this trigger condition is met
     */
    boolean test(Set<String> activeTriggers, Map<String, Double> customVariables, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed);

    MapCodec<? extends FsmTrigger> type();
}
