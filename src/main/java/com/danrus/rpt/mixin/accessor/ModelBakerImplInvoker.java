package com.danrus.rpt.mixin.accessor;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.SpriteGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ModelBakery.ModelBakerImpl.class)
public interface ModelBakerImplInvoker {
    @Invoker("<init>")
    static ModelBakery.ModelBakerImpl rpt$create(ModelBakery bakery, SpriteGetter spriteGetter) {
        throw new UnsupportedOperationException();
    }
}
