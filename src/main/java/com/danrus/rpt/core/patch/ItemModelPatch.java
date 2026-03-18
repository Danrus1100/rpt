package com.danrus.rpt.core.patch;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record ItemModelPatch(ResourceLocation template, Map<String, ItemModel.Unbaked> captures) {

    public static final Codec<ItemModelPatch> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("template").forGetter(ItemModelPatch::template)
    ).apply(inst, ItemModelPatch::new));

    public ItemModelPatch(ResourceLocation template) {
        this(template, new HashMap<>());
    }

    public void capture(String key, ItemModel.Unbaked capture) {
        captures.put(key, capture);
    }
}