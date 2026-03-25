package com.danrus.rpt.core.textures;

import net.minecraft.resources.Identifier;

@FunctionalInterface
public interface SwapApplier {
    void apply(Identifier location);
}
