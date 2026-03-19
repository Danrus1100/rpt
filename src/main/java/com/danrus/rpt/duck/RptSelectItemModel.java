package com.danrus.rpt.duck;

import com.danrus.rpt.core.item.RptField;

public interface RptSelectItemModel {
    void rpt$setParams(RptField params);
    RptField rpt$getParams();
}
