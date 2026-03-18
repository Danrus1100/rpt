package com.danrus.rpt.mixin.render.entity;

import com.danrus.rpt.duck.LivingEntityHolder;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({HumanoidRenderState.class, HumanoidModel.class})
public class LivingEntityHolderMixins implements LivingEntityHolder {

    @Unique
    private LivingEntity rpt$entity;

    @Override
    public void rpt$setEntity(LivingEntity entity) {
        this.rpt$entity = entity;
    }

    @Override
    public LivingEntity rpt$getEntity() {
        return rpt$entity;
    }
}
