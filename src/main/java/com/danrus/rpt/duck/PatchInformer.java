package com.danrus.rpt.duck;

import net.minecraft.resources.Identifier;

public interface PatchInformer {
    Identifier rpt$getPatchPath();
    void rpt$setPatchPath(Identifier path);
}
