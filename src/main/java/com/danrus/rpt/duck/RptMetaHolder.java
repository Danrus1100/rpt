package com.danrus.rpt.duck;

import com.danrus.rpt.core.meta.RptMeta;
import org.jetbrains.annotations.Nullable;

public interface RptMetaHolder {
    @Nullable RptMeta rpt$getMeta();
    void rpt$setMeta(@Nullable RptMeta meta);
}
