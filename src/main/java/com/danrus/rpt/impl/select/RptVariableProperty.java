package com.danrus.rpt.impl.select;

import com.danrus.rpt.core.item.RptField;
import com.danrus.rpt.core.item.RptVariables;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record RptVariableProperty<T>(RptVariables.Type<T> varType, String varName) implements RptSelectItemModelProperty<T> {

    public static final SelectItemModelProperty.Type<RptVariableProperty<?>, ?> TYPE = createType();

    @SuppressWarnings("unchecked")
    public static <T> SelectItemModelProperty.Type<RptVariableProperty<T>, T> castType() {
        return (SelectItemModelProperty.Type<RptVariableProperty<T>, T>) (Object) TYPE;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static SelectItemModelProperty.Type<RptVariableProperty<?>, ?> createType() {
        return (SelectItemModelProperty.Type<RptVariableProperty<?>, ?>) (SelectItemModelProperty.Type) createTyped();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> SelectItemModelProperty.Type<RptVariableProperty<T>, T> createTyped() {
        MapCodec<SelectItemModel.UnbakedSwitch<RptVariableProperty<T>, T>> mapCodec = RptVariables.Type.CODEC.dispatchMap(
                "var_type",
                unbakedSwitch -> ((RptVariableProperty<T>) unbakedSwitch.property()).varType(),
                typeRaw -> {
                    RptVariables.Type<T> type = (RptVariables.Type<T>) typeRaw;
                    return RecordCodecBuilder.mapCodec(instance -> instance.group(
                            Codec.STRING.fieldOf("var_name").forGetter(unbaked -> ((RptVariableProperty<T>) unbaked.property()).varName()),
                            SelectItemModelProperty.Type.createCasesFieldCodec((Codec<T>) type.codecOrThrow()).forGetter(unbaked -> unbaked.cases())
                    ).apply(instance, (varName, cases) -> new SelectItemModel.UnbakedSwitch<>(new RptVariableProperty<>(type, varName), cases)));
                }
        );

        return new SelectItemModelProperty.Type(mapCodec);
    }

    @Override
    public @Nullable T get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i, ItemDisplayContext itemDisplayContext, RptField params) {
        return params.variables().get(varType, varName);
    }

    @Override
    public Codec<T> valueCodec() {
        return (Codec<T>) varType.codecOrThrow();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Type<? extends SelectItemModelProperty<T>, T> type() {
        // Каст через Object решает проблему несовместимости wildcard <?> и привязанного <T>
        return (Type<? extends SelectItemModelProperty<T>, T>) (Object) TYPE;
    }
}