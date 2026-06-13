package com.tce.smart.common.core.util;


import com.tce.smart.common.core.model.Count;

import java.util.List;

/**
 * @author wxjason
 */
public interface ExcelHandle {
    /**
     * @param count
     * @param i
     * @param row
     */
    void handle(Count count, Integer i, List<Object> row);
}
