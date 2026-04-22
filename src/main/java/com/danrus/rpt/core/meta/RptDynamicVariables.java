package com.danrus.rpt.core.meta;

import java.util.HashMap;
import java.util.Map;

public class RptDynamicVariables {
    private final Map<String, Double> values = new HashMap<>();
    private final String packSource;

    public RptDynamicVariables(String packSource) {
        this.packSource = packSource;
    }

    public double get(String key) {
        return values.getOrDefault(key, 0d);
    }

    public void set(String key, double value) {
        values.put(key, value);
    }
}
