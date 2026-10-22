package com.danrus.rpt.core.template;

import com.danrus.rpt.core.item.RptField;
import com.danrus.rpt.duck.RptBakingContext;
import com.mojang.serialization.Codec;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.item.EmptyModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModels;
import org.joml.Matrix4fc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record RptTemplate(ItemModel model, RptField params, boolean needRebake) {

    private static final Logger log = LoggerFactory.getLogger(RptTemplate.class);

    public static record Unbaked(ItemModel.Unbaked unbaked, RptField params) {
        public static final Codec<Unbaked> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemModels.CODEC.fieldOf("model").forGetter(Unbaked::unbaked),
                RptField.CODEC.optionalFieldOf("rpt", RptField.EMPTY).forGetter(Unbaked::params)
        ).apply(instance, Unbaked::new));

        public RptTemplate bake(ItemModel.BakingContext context
                                , Matrix4fc transformations
        ) {
            // Template fields apply to the whole template tree, but must not leak into
            // models baked after this template in the same item context.
            RptBakingContext rptContext = RptBakingContext.class.cast(context);
            int previousSize = rptContext.rpt$addFields(params);

            boolean needRebake = false;
            ItemModel model;
            try {
                model = unbaked.bake(context
                        , transformations
                );
            } catch (Exception e) {
                log.error("Failed to bake template model; retrying on next use", e);
                needRebake = true;
                model = new EmptyModel();
            } finally {
                rptContext.rpt$truncateFields(previousSize);
            }

            return new RptTemplate(model, params, needRebake);
        }

    }
}
