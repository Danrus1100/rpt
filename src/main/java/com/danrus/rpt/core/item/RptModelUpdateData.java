package com.danrus.rpt.core.item;

import com.danrus.rpt.core.meta.RptDynamicVariables;

public record RptModelUpdateData(RptField field, RptDynamicVariables variables) {

    double findNumber(String key) {
        if (field.constants().numbers().containsKey(key)) {
            return field.constants().numbers().get(key);
        }
        return 0d;
    }

}
