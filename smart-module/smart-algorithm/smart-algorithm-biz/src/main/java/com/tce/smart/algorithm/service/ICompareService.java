package com.tce.smart.algorithm.service;

import com.tce.smart.algorithm.api.dto.req.CompareDTO;


/**
 * @ClassName: ICompareService
 * @Package com.tce.smart.algorithm.service
 * @Description: 人像比对算法
 * @Author wuxinjian
 * @Date 2019-10-10 10:12
 * @Version V1.0
 */
public interface ICompareService {


    /**
     * 人像比对算法
     * @param id
     * @param compareDTO
     * @return
     */
	com.tce.smart.algorithm.api.dto.resp.CompareDTO compare(String id, CompareDTO compareDTO);

	/**
	 * 处理器
	 * @return
	 */
	String handler();
}
