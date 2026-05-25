package com.danrus.rpt.core.meta;

import com.danrus.rpf.core.init.config.RpfConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class RptDynamicVariables {
    private final Map<ResourceLocation, Double> values = new HashMap<>();

    public double get(ResourceLocation key) {
        return values.getOrDefault(key, 0d);
    }
    public void set(ResourceLocation key, double value) {
        values.put(key, value);
    }
}
