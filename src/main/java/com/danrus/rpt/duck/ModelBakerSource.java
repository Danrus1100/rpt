package com.danrus.rpt.duck;

import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.SpriteGetter;

public interface ModelBakerSource {
    ModelBaker rpt$createModelBaker(SpriteGetter spriteGetter);
}
