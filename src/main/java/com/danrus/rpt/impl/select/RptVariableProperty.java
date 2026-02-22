package com.danrus.rpt.impl.select;

import com.danrus.rpt.core.item.RptItemVariables;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record RptVariableProperty<T>(RptItemVariables.Type<T> varType) implements SelectItemModelProperty<T> {

    public static final SelectItemModelProperty.Type<RptVariableProperty<?>, ?> TYPE = createType();

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static SelectItemModelProperty.Type<RptVariableProperty<?>, ?> createType() {
        return (SelectItemModelProperty.Type<RptVariableProperty<?>, ?>) (SelectItemModelProperty.Type) createTyped();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> SelectItemModelProperty.Type<RptVariableProperty<T>, T> createTyped() {
        MapCodec<SelectItemModel.UnbakedSwitch<RptVariableProperty<T>, T>> mapCodec = RptItemVariables.Type.CODEC.dispatchMap(
                "var_type",
                unbakedSwitch -> (RptItemVariables.Type) ((RptVariableProperty) unbakedSwitch.property()).varType(),
                typeRaw -> {
                    RptItemVariables.Type<T> type = (RptItemVariables.Type<T>) typeRaw;

                    return SelectItemModelProperty.Type.createCasesFieldCodec((Codec<T>) type.codecOrThrow()).xmap(
                            list -> new SelectItemModel.UnbakedSwitch<>(new RptVariableProperty<>(type), (java.util.List) list),
                            unbaked -> unbaked.cases()
                    );
                }
        );

        return new SelectItemModelProperty.Type(mapCodec);
    }

    @Override
    public @Nullable T get(@NotNull ItemStack itemStack, @Nullable ClientLevel clientLevel,
                           @Nullable LivingEntity livingEntity, int i,
                           @NotNull ItemDisplayContext itemDisplayContext) {
        return ;
    }

    @Override
    public Codec<T> valueCodec() {
        return (Codec<T>) varType.codecOrThrow();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Type<? extends SelectItemModelProperty<T>, T> type() {
        return (Type<? extends SelectItemModelProperty<T>, T>) TYPE;
    }
}
