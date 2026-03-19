package com.danrus.rpt.duck;

import com.danrus.rpt.core.item.RptField;

import java.util.Optional;

public interface RptItemParamsHolder {
    Optional<RptField> rpt$getParams();
    void rpt$setParams(RptField params);
    void rpt$clearParams();
}
