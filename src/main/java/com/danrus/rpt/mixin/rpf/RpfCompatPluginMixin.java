package com.danrus.rpt.mixin.rpf;

import com.danrus.rpf.compat.RpfCompatPlugin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Mixin(RpfCompatPlugin.class)
public class RpfCompatPluginMixin {
    @Shadow
    @Final
    @Mutable
    private static Map<String, String> COMPAT_CLASSES;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onInjectNewValues(CallbackInfo ci) {
        Map<String, String> newMap = new HashMap<>(COMPAT_CLASSES);
        newMap.put("iris", "com.danrus.rpt.compat.iris.IrisCompat");
        COMPAT_CLASSES = Collections.unmodifiableMap(newMap);
    }
}
