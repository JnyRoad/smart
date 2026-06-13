package com.tce.smart.platform.service;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.Led;
import com.tce.smart.platform.api.dto.QueryLedDTO;

/**
 * 显示信息
 */
public interface LedService {

    /**
     * 设置显示信息
     * @param led 显示信息
     */
    Result<Led> set(Led led);

    /**
     * 获取显示信息
     * @param queryLedDTO 显示信息
     */
	Led get(QueryLedDTO queryLedDTO);
}
