package com.danrus.rpt.mixin.accessor;

import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.SpriteGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ModelBakery.ModelBakerImpl.class)
public interface ModelBakerImplInvoker {
    @Invoker("<init>")
    static ModelBakery.ModelBakerImpl rpt$create
            //? if <=1.21.10 {
            (ModelBakery bakery, SpriteGetter spriteGetter)
            //? } else {
            /*(ModelBakery bakery, final SpriteGetter spriteGetter, final ModelBaker.PartCache partCache, final ModelBakery.MissingModels missingModels)
            *///?}
    {
        throw new UnsupportedOperationException();
    }
}
