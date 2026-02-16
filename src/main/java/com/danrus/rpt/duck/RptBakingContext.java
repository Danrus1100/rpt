package com.danrus.rpt.duck;

import com.danrus.rpt.core.RptItemParams;

public interface RptBakingContext {
    RptItemParams rpt$getParams();
    void rpt$setParams(RptItemParams params);
}
