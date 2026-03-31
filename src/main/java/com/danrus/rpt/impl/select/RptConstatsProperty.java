package com.danrus.rpt.impl.select;

import com.danrus.rpt.core.item.RptField;
import com.danrus.rpt.core.item.ItemConstants;
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

public record RptConstatsProperty<T>(ItemConstants.Type<T> varType, String varName) implements RptSelectItemModelProperty<T> {

    public static final SelectItemModelProperty.Type<RptConstatsProperty<?>, ?> TYPE = createType();

    @SuppressWarnings("unchecked")
    public static <T> SelectItemModelProperty.Type<RptConstatsProperty<T>, T> castType() {
        return (SelectItemModelProperty.Type<RptConstatsProperty<T>, T>) (Object) TYPE;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static SelectItemModelProperty.Type<RptConstatsProperty<?>, ?> createType() {
        return (SelectItemModelProperty.Type<RptConstatsProperty<?>, ?>) (SelectItemModelProperty.Type) createTyped();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> SelectItemModelProperty.Type<RptConstatsProperty<T>, T> createTyped() {
        MapCodec<SelectItemModel.UnbakedSwitch<RptConstatsProperty<T>, T>> mapCodec = ItemConstants.Type.CODEC.dispatchMap(
                "const_type",
                unbakedSwitch -> ((RptConstatsProperty<T>) unbakedSwitch.property()).varType(),
                typeRaw -> {
                    ItemConstants.Type<T> type = (ItemConstants.Type<T>) typeRaw;
                    return RecordCodecBuilder.mapCodec(instance -> instance.group(
                            Codec.STRING.fieldOf("const_name").forGetter(unbaked -> ((RptConstatsProperty<T>) unbaked.property()).varName()),
                            SelectItemModelProperty.Type.createCasesFieldCodec((Codec<T>) type.codecOrThrow()).forGetter(unbaked -> unbaked.cases())
                    ).apply(instance, (varName, cases) -> new SelectItemModel.UnbakedSwitch<>(new RptConstatsProperty<>(type, varName), cases)));
                }
        );

        return new SelectItemModelProperty.Type(mapCodec);
    }

    @Override
    public @Nullable T get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int i, ItemDisplayContext itemDisplayContext, RptField params) {
        return params.constants().get(varType, varName);
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