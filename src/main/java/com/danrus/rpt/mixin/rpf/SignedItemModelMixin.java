package com.danrus.rpt.mixin.rpf;

import com.danrus.rpf.core.item.SignedItemModel;
import com.danrus.rpt.core.item.RptItemParams;
import com.danrus.rpt.duck.RptSignedItemModel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(SignedItemModel.class)
public class SignedItemModelMixin implements RptSignedItemModel {

    @Unique
    @Nullable
    private RptItemParams rpt$params;

    @Override
    public Optional<RptItemParams> rpt$getParams() {
        return Optional.ofNullable(this.rpt$params);
    }

    @Override
    public void rpt$setParams(@Nullable RptItemParams params) {
        this.rpt$params = params;
    }
}
