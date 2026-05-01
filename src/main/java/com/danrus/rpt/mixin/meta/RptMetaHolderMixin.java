package com.danrus.rpt.mixin.meta;

import com.danrus.rpt.core.meta.RptMeta;
import com.danrus.rpt.duck.RptMetaHolder;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({ItemStack.class, PackLocationInfo.class})
public class RptMetaHolderMixin implements RptMetaHolder {

    @Unique
    @Nullable RptMeta rpt$meta;

    @Override
    public @Nullable RptMeta rpt$getMeta() {
        return rpt$meta;
    }

    @Override
    public void rpt$setMeta(@Nullable RptMeta meta) {
        this.rpt$meta = meta;
    }
}
