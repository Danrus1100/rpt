package com.danrus.rpt.duck;

import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.SpriteGetter;

public interface ModelBakerSource {
    ModelBaker rpt$createModelBaker(
            //? <26.1
            SpriteGetter sprites
            //? >=26.1
            //net.minecraft.client.resources.model.sprite.MaterialBaker sprites
    );
}
