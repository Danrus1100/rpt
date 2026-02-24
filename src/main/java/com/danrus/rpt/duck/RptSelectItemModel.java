package com.danrus.rpt.duck;

import com.danrus.rpt.core.item.RptItemParams;

public interface RptSelectItemModel {
    void rpt$setParams(RptItemParams params);
    RptItemParams rpt$getParams();
}
