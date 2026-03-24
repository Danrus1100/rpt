package com.danrus.rpt.core.bbmodel;

import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public final class BbStackIdentity {
    private final ItemStack snapshot;

    private BbStackIdentity(ItemStack stack) {
        this.snapshot = stack.copy();
    }

    public static BbStackIdentity of(ItemStack stack) {
        return new BbStackIdentity(stack);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BbStackIdentity other)) return false;
        return ItemStack.isSameItemSameComponents(snapshot, other.snapshot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(snapshot.getItem());
    }
}
