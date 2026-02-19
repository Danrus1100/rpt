package com.danrus.rpt.duck;

import com.danrus.rpt.core.item.RptItemParams;

import java.util.Optional;

public interface RptItemParamsHolder {
    Optional<RptItemParams> rpt$getParams();
    void rpt$setParams(RptItemParams params);
}
