package com.danrus.rpt.duck;

import com.danrus.rpt.core.item.RptItemParams;

public interface RptBakingContext {
    RptItemParams rpt$getParams();
    void rpt$addParams(RptItemParams... params);
}
