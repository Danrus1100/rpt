package com.danrus.rpt.core.bbmodel;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record BbModelStateIdentity(Identifier modelLocation, int seed) {
    public static BbModelStateIdentity of(Identifier modelLocation, int seed) {
        return new BbModelStateIdentity(modelLocation, seed);
    }
}
