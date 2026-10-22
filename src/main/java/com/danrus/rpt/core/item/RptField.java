package com.danrus.rpt.core.item;

import com.danrus.rpt.duck.RptFieldHolder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashSet;
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

    public boolean isEmpty() {
        return customFlags.isEmpty() && variables.isEmpty();
    }

    public RptField merge(RptField other) {
        if (other == null || other.isEmpty()) {
            return this;
        }
        if (this.isEmpty()) {
            return other;
        }

        List<String> newFlags;
        if (this.customFlags.isEmpty()) {
            newFlags = other.customFlags;
        } else if (other.customFlags.isEmpty()) {
            newFlags = this.customFlags;
        } else {
            LinkedHashSet<String> flags = new LinkedHashSet<>(this.customFlags);
            flags.addAll(other.customFlags);
            newFlags = List.copyOf(flags);
        }

        RptVariables newVariables = this.variables.merge(other.variables);
        if (newFlags.equals(this.customFlags) && newVariables.equals(this.variables)) {
            return this;
        }
        return new RptField(newFlags, newVariables);
    }

    public static RptField merge(RptField target, RptField source) {
        return target.merge(source);
    }

    public static RptField fromItemStack(ItemStack stack) {
        if (RptFieldHolder.class.isInstance(stack)) {
            RptFieldHolder holder = RptFieldHolder.class.cast(stack);
            Optional<RptField> paramsOpt = holder.rpt$getParams();
            return paramsOpt.orElse(EMPTY);
        }
        return EMPTY;
    }

    public static void putToItemStack(ItemStack stack, RptField params) {
        if (RptFieldHolder.class.isInstance(stack)) {
            RptFieldHolder.class.cast(stack).rpt$setParams(params);
        }
    }

    public boolean hasFlag(String flag) {
        return customFlags.contains(flag);
    }
}
