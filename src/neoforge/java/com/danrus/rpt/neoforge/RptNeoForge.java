package com.danrus.rpt.neoforge;

import com.danrus.rpt.Rpt;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

@Mod(value = RptNeoForge.MOD_ID, dist = Dist.CLIENT)
public final class RptNeoForge {

    public static final String MOD_ID = "rpt";

    public RptNeoForge(IEventBus modBus) {
        modBus.addListener(this::registerReloadListeners);
        Rpt.init();
    }

    private void registerReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(
                Identifier.fromNamespaceAndPath(MOD_ID, "first_person_transforms"),
                Rpt.getFpaManager()
        );
        event.addListener(
                Identifier.fromNamespaceAndPath(MOD_ID, "texture_swappers"),
                Rpt.getTextureSwappersManager()
        );
    }
}
