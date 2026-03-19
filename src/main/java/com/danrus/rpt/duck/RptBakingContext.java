package com.danrus.rpt.duck;

import com.danrus.rpt.core.item.RptField;

public interface RptBakingContext {
    RptField rpt$getParams();
    void rpt$addParams(RptField... params);
}
