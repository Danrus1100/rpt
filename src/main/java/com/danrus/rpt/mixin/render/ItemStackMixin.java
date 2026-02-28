package com.danrus.rpt.mixin.render;

import com.danrus.rpt.core.item.RptItemParams;
import com.danrus.rpt.duck.RptItemParamsHolder;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(ItemStack.class)
public class ItemStackMixin implements RptItemParamsHolder {

    @Nullable
    @Unique
    private RptItemParams rpt$params;

    @Override
    public Optional<RptItemParams> rpt$getParams() {
        return Optional.ofNullable(this.rpt$params);
    }

    @Override
    public void rpt$setParams(RptItemParams params) {
        this.rpt$params = params;
    }

    @Override
    public void rpt$clearParams() {
        this.rpt$params = RptItemParams.EMPTY;
    }
}
