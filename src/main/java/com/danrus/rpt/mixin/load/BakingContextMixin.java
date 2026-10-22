package com.danrus.rpt.mixin.load;

import com.danrus.rpt.core.item.RptField;
import com.danrus.rpt.duck.RptBakingContext;
import net.minecraft.client.renderer.item.ItemModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(ItemModel.BakingContext.class)
public class BakingContextMixin implements RptBakingContext {

    @Unique
    private final List<RptField> rpt$params = new ArrayList<>();

    @Override
    public RptField rpt$getField() {
        RptField merged = RptField.EMPTY;
        for (RptField field : rpt$params) {
            merged = merged.merge(field);
        }
        return merged;
    }

    @Override
    public int rpt$addFields(RptField... params) {
        int previousSize = this.rpt$params.size();
        this.rpt$params.addAll(Arrays.asList(params));
        return previousSize;
    }

    @Override
    public void rpt$truncateFields(int size) {
        int clampedSize = Math.max(0, Math.min(size, this.rpt$params.size()));
        if (clampedSize < this.rpt$params.size()) {
            this.rpt$params.subList(clampedSize, this.rpt$params.size()).clear();
        }
    }
}
