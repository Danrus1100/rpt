package com.danrus.rpt.duck;

import com.danrus.rpt.core.item.RptField;

public interface RptBakingContext {
    RptField rpt$getField();

    /**
     * Adds fields to the current bake scope and returns the previous size.
     */
    int rpt$addFields(RptField... params);

    /**
     * Restores the field stack to the size returned by {@link #rpt$addFields(RptField...)}.
     */
    void rpt$truncateFields(int size);
}
