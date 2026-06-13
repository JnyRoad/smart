package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 请假详情
 *
 * @author 梁圆
 * @date 2019-04-28 18:19:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchAskLeaveApplicationDetailVO extends Model<SearchAskLeaveApplicationDetailVO> {
	private static final long serialVersionUID = 1L;

	/**
	 * 请假的员工信息
	 */
	private EmployeeAskLeaveVO employee;

	/**
	 * 流程节点
	 */
	private List<FlowVO> flow;

	/**
	 * 流程id
	 */
	private String processId;

}
