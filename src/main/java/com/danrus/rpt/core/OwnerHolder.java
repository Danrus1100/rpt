package com.danrus.rpt.core;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

// TODO: move this to RPF
public class OwnerHolder {
        private final @Nullable net.minecraft.world.entity.ItemOwner owner;

    public OwnerHolder(
            @Nullable net.minecraft.world.entity.ItemOwner owner
    ) {
        this.owner = owner;
    }

    @Nullable public net.minecraft.world.entity.ItemOwner get() {return owner;}

    @Nullable
    public LivingEntity asLivingEntity() {
        return get()
        .asLivingEntity()
        ;
    }
}
