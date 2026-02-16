package com.danrus.rpt.mixin.load;

import com.danrus.rpt.core.RptItemParams;
import com.danrus.rpt.duck.RptBakingContext;
import net.minecraft.client.renderer.item.ItemModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemModel.BakingContext.class)
public class BakingContextMixin implements RptBakingContext {

    @Unique
    private RptItemParams rpt$params;

    @Override
    public RptItemParams rpt$getParams() {
        return this.rpt$params;
    }

    @Override
    public void rpt$setParams(RptItemParams params) {
        this.rpt$params = params;
    }
}
