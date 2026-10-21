package com.danrus.rpt.mixin.regs;

import com.danrus.rpt.impl.select.BiomeLocationProperty;
import com.danrus.rpt.impl.select.DifficultyTypeProperty;
import com.danrus.rpt.impl.select.RptVariableProperty;
import com.danrus.rpt.impl.select.WeatherTypeProperty;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectItemModelProperties.class)
public class SelectItemModelPropertiesMixin {
    @Shadow
    @Final
    public static ExtraCodecs.LateBoundIdMapper<Identifier, SelectItemModelProperty.Type<?, ?>> ID_MAPPER;

    @Inject(
            method = "bootstrap",
            at = @At("HEAD")
    )
    private static void rpt$bootstrap(CallbackInfo ci) {
        ID_MAPPER.put(Identifier.fromNamespaceAndPath("rpt", "variable"), RptVariableProperty.castType());
        ID_MAPPER.put(Identifier.fromNamespaceAndPath("rpt", "biome"), BiomeLocationProperty.TYPE);
        ID_MAPPER.put(Identifier.fromNamespaceAndPath("rpt", "weather"), WeatherTypeProperty.TYPE);
        ID_MAPPER.put(Identifier.fromNamespaceAndPath("rpt", "difficulty"), DifficultyTypeProperty.TYPE);
    }
}
