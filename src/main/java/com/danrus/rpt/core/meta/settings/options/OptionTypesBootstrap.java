package com.danrus.rpt.core.meta.settings.options;

import com.danrus.rpt.core.meta.settings.options.yacl.BooleanYaclOption;

public final class OptionTypesBootstrap {
    private static boolean bootstrapped;

    private OptionTypesBootstrap() {}

    public static synchronized void bootstrap() {
        if (bootstrapped) {
            return;
        }

        OptionTypes.register(BooleanYaclOption.ID, BooleanYaclOption.MAP_CODEC);
        bootstrapped = true;
    }
}
