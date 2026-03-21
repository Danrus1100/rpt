package com.danrus.rpt.mixin.render;

import com.danrus.rpt.core.item.RptField;
import com.danrus.rpt.duck.RptFieldHolder;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;

@Mixin(ItemStack.class)
public class ItemStackMixin implements RptFieldHolder {

    @Nullable
    @Unique
    private RptField rpt$params;

    @Override
    public Optional<RptField> rpt$getParams() {
        return Optional.ofNullable(this.rpt$params);
    }

    @Override
    public void rpt$setParams(RptField params) {
        this.rpt$params = params;
    }

    @Override
    public void rpt$clearParams() {
        this.rpt$params = RptField.EMPTY;
    }
}
