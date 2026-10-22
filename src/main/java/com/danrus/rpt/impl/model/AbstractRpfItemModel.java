package com.danrus.rpt.impl.model;

import com.danrus.rpf.api.RpfItemModel;
import net.minecraft.client.renderer.item.ItemModel;

public abstract class AbstractRpfItemModel implements RpfItemModel, ItemModel {

    private boolean isFallback = false;

    @Override
    public void rpf$markAsFallback() {
        this.isFallback = true;
    }

    @Override
    public boolean rpf$isFallback() {
        return isFallback;
    }
}
