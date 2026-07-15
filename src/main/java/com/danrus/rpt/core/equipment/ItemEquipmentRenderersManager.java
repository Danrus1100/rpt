package com.danrus.rpt.core.equipment;

import com.danrus.rpt.core.AbstractNestedSelectorItemsReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;

public class ItemEquipmentRenderersManager extends AbstractNestedSelectorItemsReloadListener<ItemEquipmentRenderer.Unbaked> {

    private static final FileToIdConverter LISTENER = FileToIdConverter.json("rpt/equipment");

    public ItemEquipmentRenderersManager() {
        super(LISTENER, ItemEquipmentRenderer.Unbaked.CODEC, true);
    }

    @Override
    protected String getNameOfObjective() {
        return "Equipment";
    }

    @Override
    protected ResourceLocation prepareLocation(ResourceLocation rawLocation) {
        return null;
    }
}
