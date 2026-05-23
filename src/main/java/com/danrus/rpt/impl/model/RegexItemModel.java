package com.danrus.rpt.impl.model;

import com.danrus.rpf.api.RpfItemModel;
import com.danrus.rpf.api.TestsResultCollector;
import com.danrus.rpf.api.codec.RpfModelsCodecsExtends;
import com.danrus.rpf.core.item.ModelUpdateContext;
import com.danrus.rpt.core.OwnerHolder;
import com.danrus.rpt.core.RptUnbakedModel;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

public class RegexItemModel extends AbstractRpfItemModel{

    private final Map<List<Pattern>, ItemModel> regexes;
    private final ItemModel fallback;

    public RegexItemModel(Map<List<Pattern>, ItemModel> regexes, ItemModel fallback) {
        this.regexes = regexes;
        this.fallback = fallback;
    }

    @Override
    boolean rpf$doDelegate(ModelUpdateContext context, ItemStack stack, OwnerHolder owner, @Nullable ItemModel prev, TestsResultCollector collector) {
        return RpfItemModel.class.cast(selectModelFromRegex(stack)).rpf$doDelegate(context, stack, owner.get(), prev, collector);
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack stack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, OwnerHolder owner ,int seed) {
        selectModelFromRegex(stack).update(renderState, stack, itemModelResolver, displayContext, level, owner.get(), seed);
    }

    private ItemModel selectModelFromRegex(ItemStack stack) {
        Component customNameComponent = stack.get(DataComponents.CUSTOM_NAME);
        if (customNameComponent == null) {
            return fallback;
        }
        String customName = customNameComponent.getString();

        for (var entry : regexes.entrySet()) {
            for (Pattern pattern : entry.getKey()) {
                if (pattern.matcher(customName).find()) {
                    return entry.getValue();
                }
            }
        }
        return fallback;
    }


    public static record Unbaked(Optional<ItemModel.Unbaked> fallback, List<RegexCase> regexes) implements RptUnbakedModel {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ItemModels.CODEC.optionalFieldOf("fallback").forGetter(Unbaked::fallback),
                RegexCase.CODEC.listOf().fieldOf("cases").forGetter(Unbaked::regexes)
        ).apply(instance, Unbaked::new));


        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("rpt", "regex");

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return RpfModelsCodecsExtends.getInstance().wrap(ID, MAP_CODEC);
        }

        @Override
        public @NotNull ItemModel bake(BakingContext context, Baker baker) {
            ItemModel fallbackModel = this.fallback.map(unbaked -> baker.bake(context, unbaked)).orElseGet(context::missingItemModel);
            RpfItemModel.class.cast(fallbackModel).rpf$markAsFallback();

            Map<List<Pattern>, ItemModel> regexesModels = new LinkedHashMap<>();
            for (var regexCase : regexes) {
                List<Pattern> compiledPatterns = regexCase.regexes().stream()
                        .map(Pattern::compile)
                        .toList();

                regexesModels.put(compiledPatterns, baker.bake(context, regexCase.model));
            }
            return new RegexItemModel(regexesModels, fallbackModel);
        }


        @Override
        public void resolveDependencies(Resolver resolver) {
            fallback.ifPresent(m -> m.resolveDependencies(resolver));
            for (RegexCase regexCase : regexes) {
                regexCase.model().resolveDependencies(resolver);
            }
        }

        public static record RegexCase(List<String> regexes, ItemModel.Unbaked model) {
            public static final Codec<RegexCase> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.listOf().fieldOf("when").forGetter(RegexCase::regexes),
                    ItemModels.CODEC.fieldOf("model").forGetter(RegexCase::model)
            ).apply(instance, RegexCase::new));
        }
    }
}

