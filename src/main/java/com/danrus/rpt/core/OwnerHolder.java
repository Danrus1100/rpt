package com.danrus.rpt.core;

import org.jetbrains.annotations.Nullable;

public class OwnerHolder {
        //? if <=1.21.8 {
        /*private final @Nullable LivingEntity owner;
         *///? } else {
        private final @Nullable net.minecraft.world.entity.ItemOwner owner;
        //? }

    public OwnerHolder(
            //? if <=1.21.8 {
            /*@Nullable LivingEntity owner
             *///? } else {
            @Nullable net.minecraft.world.entity.ItemOwner owner
            //? }
    ) {
        this.owner = owner;
    }

    //? if <=1.21.8 {
    /*@Nullable public LivingEntity get() {return owner;}
     *///? } else {
    @Nullable public net.minecraft.world.entity.ItemOwner get() {return owner;}
    //? }
}
