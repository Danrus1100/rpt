package com.danrus.rpt.core;

import net.minecraft.client.renderer.item.ItemModel;
import org.joml.Matrix4fc;

import java.util.function.Function;

public interface RptUnbakedModel extends ItemModel.Unbaked {

    @Override
    default ItemModel bake(ItemModel.BakingContext context
            //? >=26.1
            //, Matrix4fc transformation
    ) {
        return bake(context, (c, u) -> u.bake(c
                //? >=26.1
                //, transformation
        ));
    }

    ItemModel bake(ItemModel.BakingContext context, Baker baker);

    @FunctionalInterface
    public interface Baker {
        ItemModel bake(ItemModel.BakingContext context, ItemModel.Unbaked unbaked);
    }
}
