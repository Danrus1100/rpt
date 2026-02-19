package com.danrus.rpt.duck;

import com.danrus.rpt.core.item.RptItemParams;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface RptClientItem {
    Optional<RptItemParams> rpt$getParams();
    void rpt$setParams(@Nullable RptItemParams params);
}
