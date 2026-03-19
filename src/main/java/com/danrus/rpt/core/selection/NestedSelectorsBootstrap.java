package com.danrus.rpt.core.selection;

import com.danrus.rpt.core.selection.type.ByComponentSelector;
import com.danrus.rpt.core.selection.type.CompositeSelector;
import com.danrus.rpt.core.selection.type.EmptySelector;
import com.danrus.rpt.core.selection.type.ExpressionSelector;
import com.danrus.rpt.core.selection.type.SelectApplier;
import net.minecraft.resources.ResourceLocation;

public final class NestedSelectorsBootstrap {
    private NestedSelectorsBootstrap() {}

    public static void bootstrap() {
        NestedSelectors.register(
                ResourceLocation.fromNamespaceAndPath("rpt", "apply"),
                SelectApplier::codec
        );
        NestedSelectors.register(
                ResourceLocation.fromNamespaceAndPath("rpt", "component"),
                ByComponentSelector::codec
        );
        NestedSelectors.register(
                ResourceLocation.fromNamespaceAndPath("rpt", "empty"),
                EmptySelector::codec
        );
        NestedSelectors.register(
                ResourceLocation.fromNamespaceAndPath("rpt", "expression"),
                ExpressionSelector.Unbaked::codec
        );
        NestedSelectors.register(
                ResourceLocation.fromNamespaceAndPath("rpt", "composite"),
                CompositeSelector.Unbaked::codec
        );
    }
}
