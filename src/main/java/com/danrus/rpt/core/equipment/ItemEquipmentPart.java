package com.danrus.rpt.core.equipment;

import com.danrus.rpt.core.RptUnbakedModel;
import com.danrus.rpt.core.anchor.AnchorType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModels;

public record ItemEquipmentPart(AnchorType anchor, ItemModel model) {



    public static record Unbaked(AnchorType anchor, ItemModel.Unbaked model) {
        public static final Codec<Unbaked> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                AnchorType.CODEC.fieldOf("anchor").forGetter(Unbaked::anchor),
                ItemModels.CODEC.fieldOf("model").forGetter(Unbaked::model)
        ).apply(instance, Unbaked::new));

        public ItemEquipmentPart bake(RptUnbakedModel.Baker baker, ItemModel.BakingContext context) {
            return new ItemEquipmentPart(anchor, baker.bake(context, model));
        }
    }
}
