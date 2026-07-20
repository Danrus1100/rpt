package com.danrus.rpt.core.meta.settings.options.yacl;

import com.danrus.rpt.core.meta.settings.options.OptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.gui.controllers.TickBoxController;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class BooleanYaclOption extends YaclOption<Boolean> {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("rpt", "boolean");
    private static final MapCodec<BaseData<Boolean>> BASE_MAP_CODEC = baseMapCodec(Codec.BOOL, false);
    public static final MapCodec<BooleanYaclOption> MAP_CODEC = BASE_MAP_CODEC.xmap(
            data -> new BooleanYaclOption(
                    data.name(),
                    data.description(),
                    data.descriptionImage(),
                    data.defaultValue()
            ),
            BooleanYaclOption::baseData
    );

    public BooleanYaclOption(Component name, @Nullable Component description,
                             @Nullable ResourceLocation descriptionImage, boolean defaultValue) {
        super(Codec.BOOL, defaultValue, name, description, descriptionImage);
    }

    public BooleanYaclOption(Component name, @Nullable Component description,
                             @Nullable ResourceLocation descriptionImage) {
        this(name, description, descriptionImage, false);
    }

    @Override
    public MapCodec<? extends OptionType<?>> type() {
        return MAP_CODEC;
    }

    @Override
    protected Controller<Boolean> createCustomController(Option<Boolean> option) {
        return new TickBoxController(option);
    }
}
