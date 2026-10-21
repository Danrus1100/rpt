package com.danrus.rpt.fabric;

import com.danrus.rpt.Rpt;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

public final class RptFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ResourceManagerHelper helper = ResourceManagerHelper.get(PackType.CLIENT_RESOURCES);
        helper.registerReloadListener(new FabricReloadListener(
                Identifier.fromNamespaceAndPath("rpt", "first_person_transforms"),
                Rpt.getFpaManager()
        ));
        helper.registerReloadListener(new FabricReloadListener(
                Identifier.fromNamespaceAndPath("rpt", "texture_swappers"),
                Rpt.getTextureSwappersManager()
        ));

        Rpt.init();
    }
}
