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
    private List<RptField> rpt$params = new ArrayList<>();

    @Override
    public RptField rpt$getField() {
        if (rpt$params.isEmpty()) return RptField.EMPTY;

        RptField initial = rpt$params.getFirst();

        if (rpt$params.size() == 1) {
            return initial;
        }

        for (int i = 1; i < this.rpt$params.size(); i++) {
            initial.merge(rpt$params.get(i));
        }
        return initial;
    }

    @Override
    public void rpt$addFields(RptField... params) {
        this.rpt$params.addAll(Arrays.asList(params));
    }
}
