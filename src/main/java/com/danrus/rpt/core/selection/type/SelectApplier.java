package com.danrus.rpt.core.selection.type;

import com.danrus.rpt.core.selection.NestedSelector;
import com.danrus.rpt.core.selection.SelectionContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public record SelectApplier<T>(T value) implements NestedSelector<T>, NestedSelector.SimpleUnbaked<T> {

    public static synchronized <T> MapCodec<SelectApplier<T>> codec(Codec<T> valueCodec) {
        return RecordCodecBuilder.mapCodec(i -> i.group(
                valueCodec.fieldOf("value").forGetter(applier -> applier.value())
        ).apply(i, SelectApplier::new));
    }

    @Override
    public void resolveSelect(ItemStack stack, @Nullable LivingEntity entity, SelectionContext<T> context) {
        context.applyValue(value);
    }

    @Override
    public MapCodec<? extends NestedSelector.Unbaked<T>> type(Codec<T> valueCodec) {
        return codec(valueCodec);
    }

    @Override
    public NestedSelector<T> bake() {
        return this;
    }
}
