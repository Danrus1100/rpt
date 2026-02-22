package com.danrus.rpt.plugin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class Rpt12111MixinPlugin implements IMixinConfigPlugin {
    private static final Logger log = LoggerFactory.getLogger(Rpt12111MixinPlugin.class);

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String raw = FabricLoader.getInstance().getRawGameVersion();

        String clean = raw.split("-")[0].replaceAll("[^0-9.]", "");
        String[] parts = clean.split("\\.");

        try {
            int major = Integer.parseInt(parts[0]);
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

            if (major >= 26) return true;
            if (major == 1) {
                if (minor > 21) return true;
                if (minor == 21 && patch >= 11) return true;
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
