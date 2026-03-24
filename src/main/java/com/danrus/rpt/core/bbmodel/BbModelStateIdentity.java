package com.danrus.rpt.core.bbmodel;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BbModelStateIdentity {
    private final List<Object> elements = new ArrayList<>();

    private BbModelStateIdentity() {}

    public BbModelStateIdentity(ResourceLocation modelLocation, int seed) {
        elements.add(modelLocation);
        elements.add(seed);
    }

    public void addElement(Object... element) {
        elements.addAll(List.of(element));
    }

    public static BbModelStateIdentity of(ResourceLocation modelLocation, int seed) {
        return new BbModelStateIdentity(modelLocation, seed);
    }

    public boolean containsElement(Object element) {
        return elements.contains(element);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BbModelStateIdentity that)) return false;
        return Objects.equals(elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(elements);
    }
}
