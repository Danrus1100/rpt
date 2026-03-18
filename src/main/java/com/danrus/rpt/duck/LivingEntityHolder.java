package com.danrus.rpt.duck;

import net.minecraft.world.entity.LivingEntity;

public interface LivingEntityHolder {
    void rpt$setEntity(LivingEntity entity);
    LivingEntity rpt$getEntity();
}
