package com.danrus.rpt.mixin.regs;

import com.danrus.rpt.impl.model.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModels.class)
public class ItemModelsMixin {
    @Shadow
    @Final
    public static ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends ItemModel.Unbaked>> ID_MAPPER;

    @Inject(
            method = "bootstrap",
            at = @At("HEAD")
    )
    private static void rpt$injectBootstrap(CallbackInfo ci) {
        ID_MAPPER.put(TemplateItemModel.Unbaked.ID, TemplateItemModel.Unbaked.MAP_CODEC);
        ID_MAPPER.put(VariableItemModelWrapper.Unbaked.ID, VariableItemModelWrapper.Unbaked.MAP_CODEC);
        ID_MAPPER.put(RegexItemModel.Unbaked.ID, RegexItemModel.Unbaked.MAP_CODEC);
        ID_MAPPER.put(ArmTransformWrapper.Unbaked.ID, ArmTransformWrapper.Unbaked.MAP_CODEC);
        ID_MAPPER.put(ExpressionToExpressionsModel.Unbaked.ID, ExpressionToExpressionsModel.Unbaked.MAP_CODEC);
        ID_MAPPER.put(CustomAnchorWrapper.Unbaked.ID, CustomAnchorWrapper.Unbaked.MAP_CODEC);
    }
}
