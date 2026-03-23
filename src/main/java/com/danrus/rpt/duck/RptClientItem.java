package com.danrus.rpt.duck;

import com.danrus.rpt.core.item.RptField;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface RptClientItem {
    Optional<RptField> rpt$getField();
    void rpt$setField(@Nullable RptField params);
}
