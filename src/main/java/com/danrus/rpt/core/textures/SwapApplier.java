package com.danrus.rpt.core.textures;

import net.minecraft.resources.ResourceLocation;

@FunctionalInterface
public interface SwapApplier {
    void apply(ResourceLocation location);
}
