package com.danrus.rpt.mixin.accessor;


import net.minecraft.client.resources.model.ModelBakery;
import org.spongepowered.asm.mixin.Mixin;

//? >= 26.1 {

/*import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ModelBakery.InternerImpl.class)
public interface InternerImplInvoker {
    @Invoker("<init>")
    static ModelBakery.InternerImpl rpt$create() {
        throw new UnsupportedOperationException();
    }
}
*///? } else {
@Mixin(ModelBakery.class)
public interface InternerImplInvoker {}
//?}