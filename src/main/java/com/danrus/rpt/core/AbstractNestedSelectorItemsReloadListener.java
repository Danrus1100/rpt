package com.danrus.rpt.core;

import com.danrus.rpt.core.selection.NestedSelector;
import com.danrus.rpt.core.selection.NestedSelectors;
import com.danrus.rpt.core.selection.SelectionContext;
import com.danrus.rpt.core.selection.type.EmptySelector;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.util.*;
import java.util.function.Consumer;

public abstract class AbstractNestedSelectorItemsReloadListener<T> extends SimplePreparableReloadListener<Map<Identifier, List<NestedSelector<T>>>> {

    private static final Logger log = LoggerFactory.getLogger(AbstractNestedSelectorItemsReloadListener.class);
    private final FileToIdConverter listener;
    private final Codec<T> codec;
    private final boolean ignoreFallback;

    private volatile Map<Identifier, List<NestedSelector<T>>> values = Map.of();

    public AbstractNestedSelectorItemsReloadListener(FileToIdConverter listener, Codec<T> codec, boolean ignoreFallback) {
        this.listener = listener;
        this.codec = codec;
        this.ignoreFallback = ignoreFallback;
    }

    @Override
    protected Map<Identifier, List<NestedSelector<T>>> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<Identifier, List<NestedSelector<T>>> map = new HashMap<>();
        for (Map.Entry<Identifier, List<Resource>> entry : listener.listMatchingResourceStacks(resourceManager).entrySet()) {
            List<NestedSelector<T>> list = new ArrayList<>(entry.getValue().size());
            for (Resource resource : entry.getValue()) {
                list.add(parseResource(entry.getKey(), resource));
            }
            map.put(entry.getKey(), list);
        }
        return map;
    }

    private NestedSelector<T> parseResource(Identifier location, Resource resource) {
        try (Reader reader = resource.openAsReader()) {
            NestedSelector.BakeResult<T> result = NestedSelectors.codec(codec).parse(JsonOps.INSTANCE, StrictJsonParser.parse(reader)).getOrThrow().bakeResult();
            // FIXME: it can lost info about fallbacks if root hasn't fallback
            if (result.hasFallbacks() && !ignoreFallback) {
                log.warn("{} {} from pack {} has \"fallback\". This field will be ignored.", getNameOfObjective(), location, resource.sourcePackId());
            }
            return result.selector();
        } catch (Exception e) {
            log.error("Can't read {} {} from {}: {}", getNameOfObjective().toLowerCase(), location, resource.sourcePackId(), e);
            return EmptySelector.instance();
        }
    }

    @Override
    protected void apply(Map<Identifier, List<NestedSelector<T>>> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        values = object;
    }

    protected abstract String getNameOfObjective();
    protected abstract Identifier prepareLocation(Identifier rawLocation);

    protected Optional<List<NestedSelector<T>>> getSelectors(Identifier rawLocation) {
        return Optional.ofNullable(values.get(prepareLocation(rawLocation)));
    }


    public void callback(Identifier rawLocation, ItemStack stack, @Nullable LivingEntity entity, T original, Consumer<T> callback) {

        List<NestedSelector<T>> selectors = values.get(prepareLocation(rawLocation));
        if (selectors == null || selectors.isEmpty()) {
            callback.accept(original);
            return;
        }

        boolean useFallbacks = !ignoreFallback;

        for (NestedSelector<T> selector : selectors) {
            if (selector == null) continue;

            FirstMatchConsumer<T> consumer = new FirstMatchConsumer<>(callback);

            selector.resolveSelect(stack, entity, new SelectionContext<>(useFallbacks, consumer));

            if (consumer.matched()) {
                return;
            }
        }

        callback.accept(original);
    }

    private static class FirstMatchConsumer<T> implements Consumer<T> {
        private final Consumer<T> downstream;
        private boolean matched = false;

        FirstMatchConsumer(Consumer<T> downstream) {
            this.downstream = downstream;
        }

        @Override
        public void accept(T t) {
            matched = true;
            downstream.accept(t);
        }

        public boolean matched() {
            return matched;
        }
    }

}
