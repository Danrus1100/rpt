package com.danrus.rpt.core.template;

import com.danrus.rpt.core.item.RptField;
import com.danrus.rpt.duck.RptBakingContext;
import com.mojang.serialization.Codec;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.item.EmptyModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModels;

public record RptTemplate(ItemModel model, RptField params, boolean needRebake) {

    public static record Unbaked(ItemModel.Unbaked unbaked, RptField params) {
        public static final Codec<Unbaked> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemModels.CODEC.fieldOf("model").forGetter(Unbaked::unbaked),
                RptField.CODEC.optionalFieldOf("rpt", RptField.EMPTY).forGetter(Unbaked::params)
        ).apply(instance, Unbaked::new));

        public RptTemplate bake(ItemModel.BakingContext context) {
            // adding params to context to include current params to children's params
            RptBakingContext rptContext = RptBakingContext.class.cast(context);
            rptContext.rpt$addFields(params);
            RptField merged = rptContext.rpt$getField();

            boolean needRebake = false;
            ItemModel model;
            try {
                model = unbaked.bake(context);
            } catch (Exception e) {
                needRebake = true;
                model = new EmptyModel();
            }

            return new RptTemplate(model, merged, needRebake);
        }

    }
}
