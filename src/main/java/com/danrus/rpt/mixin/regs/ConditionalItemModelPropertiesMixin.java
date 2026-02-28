package com.danrus.rpt.mixin.regs;

import com.danrus.rpt.impl.conditional.EntityFlagProperty;
import com.danrus.rpt.impl.conditional.HasFlagProperty;
import com.danrus.rpt.impl.conditional.InFluidProperty;
import com.danrus.rpt.impl.conditional.MatchCustomNameRegexProperty;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConditionalItemModelProperties.class)
public class ConditionalItemModelPropertiesMixin {

    @Shadow
    @Final
    public static ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends ConditionalItemModelProperty>> ID_MAPPER;

    @Inject(
            method = "bootstrap",
            at = @At("HEAD")
    )
    private static void rpt$injectBootstrap(CallbackInfo ci) {
        ID_MAPPER.put(ResourceLocation.fromNamespaceAndPath("rpt", "has_flag"), HasFlagProperty.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.fromNamespaceAndPath("rpt", "match"), MatchCustomNameRegexProperty.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.fromNamespaceAndPath("rpt", "in_fluid"), InFluidProperty.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.fromNamespaceAndPath("rpt", "entity_flag"), EntityFlagProperty.MAP_CODEC);
    }
}
