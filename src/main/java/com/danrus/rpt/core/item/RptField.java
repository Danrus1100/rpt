package com.danrus.rpt.core.item;

import com.danrus.rpt.duck.RptFieldHolder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record RptField(List<String> customFlags, RptVariables variables) {
    public static final Codec<RptField> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(Codec.STRING)
                    .optionalFieldOf("custom_flags", List.of())
                    .forGetter(RptField::customFlags),
            RptVariables.CODEC
                    .optionalFieldOf("variables", RptVariables.EMPTY)
                    .forGetter(RptField::variables)
    ).apply(instance, RptField::new));

    public static final RptField EMPTY = new RptField(List.of(), RptVariables.EMPTY);

    public RptField merge(RptField other) {
        List<String> newFlags = new ArrayList<>(this.customFlags);
        newFlags.addAll(other.customFlags);

        RptVariables newVariables = this.variables.merge(other.variables);

        return new RptField(List.copyOf(newFlags), newVariables);
    }

    public static RptField merge(RptField target, RptField source) {
        return target.merge(source);
    }

    public static RptField fromItemStack(ItemStack stack) {
        try {
            RptFieldHolder holder = RptFieldHolder.class.cast(stack);
            Optional<RptField> paramsOpt = holder.rpt$getParams();
            return paramsOpt.orElse(EMPTY);
        } catch (ClassCastException e) {
            return EMPTY;
        }
    }

    public static void putToItemStack(ItemStack stack, RptField params) {
        try {
            RptFieldHolder holder = RptFieldHolder.class.cast(stack);
            holder.rpt$setParams(params);
        } catch (ClassCastException e) {
            // Ignore
        }
    }

    public boolean hasFlag(String flag) {
        return !customFlags.isEmpty() && customFlags.contains(flag);
    }
}
