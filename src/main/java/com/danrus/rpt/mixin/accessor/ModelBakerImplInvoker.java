package com.danrus.rpt.mixin.accessor;

import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ModelBakery.ModelBakerImpl.class)
public interface ModelBakerImplInvoker {
    @Invoker("<init>")
    static ModelBakery.ModelBakerImpl rpt$create
            (ModelBakery bakery, final MaterialBaker materials, final ModelBaker.Interner interner, final ModelBakery.MissingModels missingModels)
    {
        throw new UnsupportedOperationException();
    }
}
