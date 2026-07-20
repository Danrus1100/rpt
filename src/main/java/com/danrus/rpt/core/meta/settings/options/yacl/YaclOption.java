package com.danrus.rpt.core.meta.settings.options.yacl;

import com.danrus.rpt.core.meta.settings.options.OptionType;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.yacl3.api.Binding;
import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.debug.DebugProperties;
import dev.isxander.yacl3.gui.image.ImageRenderer;
import dev.isxander.yacl3.gui.image.ImageRendererFactory;
import dev.isxander.yacl3.gui.image.ImageRendererManager;
import dev.isxander.yacl3.gui.utils.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public abstract class YaclOption<T> implements OptionType<T> {
    private final Codec<T> valueCodec;
    private final T defaultValue;
    private final Component name;
    @Nullable
    private final Component description;
    @Nullable
    private final ResourceLocation descriptionImage;
    private final List<BiConsumer<T, T>> hooks = new ArrayList<>();
    private T value;

    protected YaclOption(Codec<T> valueCodec, T defaultValue, Component name,
                         @Nullable Component description, @Nullable ResourceLocation descriptionImage) {
        this.valueCodec = Objects.requireNonNull(valueCodec, "valueCodec");
        this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
        this.value = defaultValue;
        this.name = Objects.requireNonNull(name, "name");
        this.description = description;
        this.descriptionImage = descriptionImage;
    }

    protected static <T> MapCodec<BaseData<T>> baseMapCodec(Codec<T> valueCodec, T fallbackDefault) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                ComponentSerialization.CODEC.fieldOf("name")
                        .forGetter((BaseData<T> data) -> data.name()),
                ComponentSerialization.CODEC.optionalFieldOf("description")
                        .forGetter(data -> Optional.ofNullable(data.description())),
                ResourceLocation.CODEC.optionalFieldOf("description_image")
                        .forGetter(data -> Optional.ofNullable(data.descriptionImage())),
                valueCodec.optionalFieldOf("default_value", fallbackDefault)
                        .forGetter(data -> data.defaultValue())
        ).apply(instance, (name, description, descriptionImage, defaultValue) -> new BaseData<>(
                name,
                description.orElse(null),
                descriptionImage.orElse(null),
                defaultValue
        )));
    }

    protected final BaseData<T> baseData() {
        return new BaseData<>(name, description, descriptionImage, defaultValue);
    }

    public final Option<T> createYaclOption() {
        Binding<T> binding = Binding.generic(defaultValue, this::getValue, this::setValue);
        return createYaclOption(binding).build();
    }

    protected Option.Builder<T> createYaclOption(Binding<T> binding) {
        OptionDescription.Builder descriptionBuilder = OptionDescription.createBuilder();

        if (description != null) {
            descriptionBuilder.text(description);
        }
        if (descriptionImage != null) {
            descriptionBuilder.customImage(Image.load(descriptionImage));
        }

        Option.Builder<T> builder = Option.<T>createBuilder()
                .name(name)
                .customController(this::createCustomController)
                .binding(binding)
                .description(descriptionBuilder.build());

        configureAdditionalOptionSettings(builder);
        return builder;
    }

    protected void configureAdditionalOptionSettings(Option.Builder<T> builder) {}

    protected abstract Controller<T> createCustomController(Option<T> option);

    @Override
    public final Codec<T> valueCodec() {
        return valueCodec;
    }

    @Override
    public final T getValue() {
        return value;
    }

    @Override
    public final T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public final void setValue(T value) {
        T newValue = Objects.requireNonNull(value, "value");
        T oldValue = this.value;
        if (Objects.equals(oldValue, newValue)) {
            return;
        }

        this.value = newValue;
        for (BiConsumer<T, T> hook : List.copyOf(hooks)) {
            hook.accept(oldValue, newValue);
        }
    }

    public final void resetValue() {
        setValue(defaultValue);
    }

    @Override
    public final void hook(BiConsumer<T, T> onChange) {
        hooks.add(Objects.requireNonNull(onChange, "onChange"));
    }

    public final Component getName() {
        return name;
    }

    public final @Nullable Component getDescription() {
        return description;
    }

    public final @Nullable ResourceLocation getDescriptionImage() {
        return descriptionImage;
    }

    protected record BaseData<T>(Component name, @Nullable Component description,
                                 @Nullable ResourceLocation descriptionImage, T defaultValue) {}

    public record Image(ResourceLocation location, int width, int height) implements ImageRenderer {
        public Image {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("Image dimensions must be positive");
            }
        }

        public static CompletableFuture<Optional<ImageRenderer>> load(ResourceLocation location) {
            return ImageRendererManager.<ImageRenderer>registerOrGetImage(
                    location,
                    () -> createFactory(location)
            ).thenApply(Optional::of);
        }

        private static ImageRendererFactory createFactory(ResourceLocation location) {
            return () -> {
                Resource resource = Minecraft.getInstance()
                        .getResourceManager()
                        .getResource(location)
                        .orElseThrow(() -> new IllegalArgumentException("Unknown image resource: " + location));

                try (var stream = resource.open(); NativeImage image = NativeImage.read(stream)) {
                    int width = image.getWidth();
                    int height = image.getHeight();
                    return () -> new Image(location, width, height);
                }
            };
        }

        @Override
        public int render(GuiGraphics graphics, int x, int y, int renderWidth, float tickDelta) {
            float ratio = renderWidth / (float) width;
            int targetHeight = (int) (height * ratio);

            GuiUtils.pushPose(graphics);
            GuiUtils.translate2D(graphics, x, y);
            GuiUtils.scale2D(graphics, ratio, ratio);
            GuiUtils.blitGuiTex(
                    graphics,
                    location,
                    0, 0,
                    0, 0,
                    width, height,
                    width, height,
                    DebugProperties.IMAGE_FILTERING
            );
            GuiUtils.popPose(graphics);

            return targetHeight;
        }

        @Override
        public void close() {}
    }
}
