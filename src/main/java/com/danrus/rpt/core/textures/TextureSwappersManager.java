package com.danrus.rpt.core.textures;

import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextureSwappersManager extends SimplePreparableReloadListener<Map<ResourceLocation, List<TextureSwapper>>> implements IdentifiableResourceReloadListener {
    private static final Logger log = LoggerFactory.getLogger(TextureSwappersManager.class);
    private Map<ResourceLocation, List<TextureSwapper>> swappers = new HashMap<>();
    private final FileToIdConverter LISTENER = FileToIdConverter.json("rpt/swappers");

    @Override
    protected @NotNull Map<ResourceLocation, List<TextureSwapper>> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, List<TextureSwapper>> map = new HashMap<>();
        for (Map.Entry<ResourceLocation, List<Resource>> entry : LISTENER.listMatchingResourceStacks(resourceManager).entrySet()) {
            List<TextureSwapper> list = new ArrayList<>(entry.getValue().size());
            for (Resource resource : entry.getValue()) {
                list.add(parseResource(entry.getKey(), resource));
            }
            map.put(entry.getKey(), list);
        }
        return map;
    }


    private TextureSwapper parseResource(ResourceLocation location, Resource resource) {
        try (Reader reader = resource.openAsReader()) {
            return TextureSwappers.CODEC.parse(JsonOps.INSTANCE, StrictJsonParser.parse(reader)).getOrThrow();
        } catch (Exception e) {
            log.error("Can't read swapper {} from {}: {}", location, resource.sourcePackId(), e);
            return null;
        }
    }

    @Override
    protected void apply(Map<ResourceLocation, List<TextureSwapper>> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        swappers = object;
    }

    public ResourceLocation swap(ResourceLocation original, ItemStack stack, @Nullable LivingEntity entity) {

        String[] paths = original.getPath().replace(".png", ".json").split("/");
        String fileName = paths[paths.length-1];

        ResourceLocation swapperLocation = ResourceLocation.fromNamespaceAndPath(
                original.getNamespace(),
                fileName
        );

        List<TextureSwapper> swapperList = swappers.get(swapperLocation);
        if (swapperList == null) return original;
        for (TextureSwapper swapper : swapperList) {
            if (swapper == null) continue;
            ResourceLocation location = swapper.swap(stack, entity);
            if (location != null) {
                return location;
            }
        }
        return original;
    }

    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath("rpt", "texture_swappers");
    }
}
