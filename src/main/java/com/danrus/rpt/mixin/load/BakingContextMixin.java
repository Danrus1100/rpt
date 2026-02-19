package com.danrus.rpt.mixin.load;

import com.danrus.rpt.core.item.RptItemParams;
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
    private List<RptItemParams> rpt$params = new ArrayList<>();

    @Override
    public RptItemParams rpt$getParams() {
        if (rpt$params.isEmpty()) return RptItemParams.EMPTY;

        RptItemParams initial = rpt$params.getFirst();

        if (rpt$params.size() == 1) {
            return initial;
        }

        for (int i = 1; i < this.rpt$params.size(); i++) {
            initial.merge(rpt$params.get(i));
        }
        return initial;
    }

    @Override
    public void rpt$addParams(RptItemParams... params) {
        this.rpt$params.addAll(Arrays.asList(params));
    }
}
