package com.tce.smart.platform.core.vo;

import java.util.List;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 调休详情
 *
 * @author 梁圆
 * @date 2019-04-28 18:19:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchBreakoffApplicationDetailVO extends BaseVO {
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	private EmployeeBreakOffVO employee;

	private List<FlowVO> flow;
}
