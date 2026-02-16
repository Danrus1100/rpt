package com.danrus.rpt.duck;

import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ModelBaker;

public interface BakingContextSource {
    ItemModel.BakingContext rpt$createBakingContext(ModelBaker baker);
}
