package com.danrus.rpt.mixin.accessor;

import net.minecraft.client.resources.model.ModelBakery;
import org.spongepowered.asm.mixin.Mixin;

//? >= 1.21.11 {
/*
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ModelBakery.PartCacheImpl.class)
public interface PartCacheImplInvoker {
    @Invoker("<init>")
    static ModelBakery.PartCacheImpl rpt$create() {
        throw new UnsupportedOperationException();
    }
}
*///? } else {
@Mixin(ModelBakery.class)
public interface PartCacheImplInvoker {}
//?}
