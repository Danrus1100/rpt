package com.danrus.rpt.mixin.regs;

import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(SelectItemModelProperties.class)
public class SelectItemModelPropertiesMixin {
    @Inject(
            method = "bootstrap"
    )
}
