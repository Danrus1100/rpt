package com.danrus.rpt.mixin.meta;

import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({ItemStack.class, PackLocationInfo.class})
public class RptMetaHolderMixin {
}
