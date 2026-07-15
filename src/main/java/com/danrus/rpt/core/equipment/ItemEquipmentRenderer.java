package com.danrus.rpt.core.equipment;

import com.danrus.rpt.core.RptUnbakedModel;
import com.danrus.rpt.core.anchor.AnchorType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModels;

import java.util.List;

public record ItemEquipmentRenderer(List<ItemEquipmentPart> parts) {



    public static record Unbaked(List<ItemEquipmentPart.Unbaked> parts) {

        public static final Codec<Unbaked> CODEC = RecordCodecBuilder.create(i -> i.group(
                ItemEquipmentPart.Unbaked.CODEC.listOf().fieldOf("parts").forGetter(Unbaked::parts)
        ).apply(i, Unbaked::new));

        public ItemEquipmentRenderer bake(RptUnbakedModel.Baker baker, ItemModel.BakingContext context) {
            return new ItemEquipmentRenderer(parts.stream().map(u -> u.bake(baker, context)).toList());
        }
    }
}
