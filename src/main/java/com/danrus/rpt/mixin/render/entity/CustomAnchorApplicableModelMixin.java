package com.danrus.rpt.mixin.render.entity;

import com.danrus.rpt.core.anchor.AnchorType;
import com.danrus.rpt.duck.CustomAnchorApplicableModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.object.armorstand.ArmorStandModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({
        PlayerModel.class,
        ArmorStandModel.class
})
public class CustomAnchorApplicableModelMixin implements CustomAnchorApplicableModel {
    @Override
    public void rpt$poseToAnchor(AnchorType anchor, PoseStack poseStack) {
        HumanoidModel<?> self = (HumanoidModel<?>) (Object) this;
        anchor.getPart(self).translateAndRotate(poseStack);
    }
}
