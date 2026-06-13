package com.tce.smart.platform.core.model;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tce.smart.platform.core.vo.EmployeeBreakOffVO;
import com.tce.smart.platform.core.vo.FlowVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 调休详情
 *
 * @author 梁圆
 * @date 2019-04-28 18:19:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchBreakoffApplicationDetail extends Model<SearchBreakoffApplicationDetail> {
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	private EmployeeBreakOffVO employee;

	private List<FlowVO> flow;

	/**
	 * 流程id
	 */
	private String processId;
}
