package com.danrus.rpt.duck;

import com.danrus.rpt.core.item.RptField;

public interface RptBakingContext {
    RptField rpt$getField();
    void rpt$addFields(RptField... params);
}
