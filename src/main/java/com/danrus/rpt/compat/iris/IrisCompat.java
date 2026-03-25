package com.danrus.rpt.compat.iris;

import com.danrus.rpf.compat.RpfCompatInitializer;
import net.irisshaders.iris.api.v0.IrisApi;

public class IrisCompat implements RpfCompatInitializer {
    @Override
    public void init() {
        IrisCompatBridge.isShadowsPass = () -> IrisApi.getInstance().isRenderingShadowPass();
    }
}
