package com.tce.smart.app.vo.fore;

import java.util.List;

import com.tce.smart.app.api.vo.SearchAdjustVO;

import lombok.Data;

/**
 *员工可调休的天数
 * @author ly
 *
 */
@Data
public class AdjustVo {

	/**
	 * 调休天数
	 */
	private Double dayCount;

	/**
	 * 集合
	 */
    private List<SearchAdjustVO> records;
}
