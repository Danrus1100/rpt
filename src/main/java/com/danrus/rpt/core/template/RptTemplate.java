package com.danrus.rpt.core.template;

import com.danrus.rpt.core.RptItemParams;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.resources.ResourceLocation;

public record RptTemplate(ItemModel model, RptItemParams params) {

    public static record Unbaked(ItemModel.Unbaked unbaked, RptItemParams params) {
        public static final Codec<Unbaked> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemModels.CODEC.fieldOf("model").forGetter(Unbaked::unbaked),
                RptItemParams.CODEC.fieldOf("rpt").forGetter(Unbaked::params)
        ).apply(instance, Unbaked::new));

        public RptTemplate bake(ItemModel.BakingContext context) {
            ItemModel model = unbaked.bake(context);
            return new RptTemplate(model, params);
        }

    }
}
