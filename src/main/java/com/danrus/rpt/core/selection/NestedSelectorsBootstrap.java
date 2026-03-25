package com.danrus.rpt.core.selection;

import com.danrus.rpt.core.selection.type.ByComponentSelector;
import com.danrus.rpt.core.selection.type.CompositeSelector;
import com.danrus.rpt.core.selection.type.EmptySelector;
import com.danrus.rpt.core.selection.type.ExpressionSelector;
import com.danrus.rpt.core.selection.type.SelectApplier;
import net.minecraft.resources.Identifier;

public final class NestedSelectorsBootstrap {
    private NestedSelectorsBootstrap() {}

    public static void bootstrap() {
        NestedSelectors.register(
                Identifier.fromNamespaceAndPath("rpt", "apply"),
                SelectApplier::codec
        );
        NestedSelectors.register(
                Identifier.fromNamespaceAndPath("rpt", "component"),
                ByComponentSelector::codec
        );
        NestedSelectors.register(
                Identifier.fromNamespaceAndPath("rpt", "empty"),
                EmptySelector::codec
        );
        NestedSelectors.register(
                Identifier.fromNamespaceAndPath("rpt", "expression"),
                ExpressionSelector.Unbaked::codec
        );
        NestedSelectors.register(
                Identifier.fromNamespaceAndPath("rpt", "composite"),
                CompositeSelector.Unbaked::codec
        );
    }
}
