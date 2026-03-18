package com.danrus.rpt.core.patch;

import net.minecraft.client.renderer.item.ItemModel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PatchesCollector {
    private final List<ItemModelPatch> patches = new ArrayList<>();

    public void addPatch(ItemModelPatch patch) {
        patches.add(patch);
    }

    @Nullable
    public ItemModel.Unbaked getCapture(String key) {
        for (var p : patches) {
            if (p.captures().containsKey(key)) {
                return p.captures().get(key);
            }
        }
        return null;
    }
}
