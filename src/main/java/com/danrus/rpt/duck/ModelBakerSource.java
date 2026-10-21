package com.danrus.rpt.duck;

import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.sprite.SpriteGetter;

public interface ModelBakerSource {
    ModelBaker rpt$createModelBaker(
            net.minecraft.client.resources.model.sprite.MaterialBaker sprites
    );
}
