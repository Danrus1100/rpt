package com.danrus.rpt.core.item;

import com.danrus.rpt.duck.RptItemParamsHolder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public record RptItemParams(List<String> customFlags, RptItemVariables variables) {
    public static final Codec<RptItemParams> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(Codec.STRING)
                    .optionalFieldOf("custom_flags", List.of())
                    .forGetter(RptItemParams::customFlags),
            RptItemVariables.CODEC
                    .optionalFieldOf("variables", RptItemVariables.EMPTY)
                    .forGetter(RptItemParams::variables)
    ).apply(instance, RptItemParams::new));

    public static final RptItemParams EMPTY = new RptItemParams(List.of(), RptItemVariables.EMPTY);

    public void merge(RptItemParams other) {
        customFlags.addAll(other.customFlags);
        variables.merge(other.variables);
    }

    public static RptItemParams merge(RptItemParams target, RptItemParams source) {
        target.merge(source);
        return target;
    }

    public static RptItemParams fromItemStack(ItemStack stack) {
        try {
            RptItemParamsHolder holder = RptItemParamsHolder.class.cast(stack);
            Optional<RptItemParams> paramsOpt = holder.rpt$getParams();
            return paramsOpt.orElse(EMPTY);
        } catch (ClassCastException e) {
            return EMPTY;
        }
    }

    public static void putToItemStack(ItemStack stack, RptItemParams params) {
        try {
            RptItemParamsHolder holder = RptItemParamsHolder.class.cast(stack);
            holder.rpt$setParams(params);
        } catch (ClassCastException e) {
            // Ignore
        }
    }

    public boolean hasFlag(String flag) {
        return !customFlags.isEmpty() && customFlags.contains(flag);
    }
}
