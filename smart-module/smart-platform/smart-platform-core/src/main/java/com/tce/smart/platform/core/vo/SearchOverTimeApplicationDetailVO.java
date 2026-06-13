package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 加班详情
 *
 * @author 梁圆
 * @date 2019-04-28 18:19:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchOverTimeApplicationDetailVO extends Model<SearchOverTimeApplicationDetailVO> {
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	private EmployeeOverTimeVO employee;

	private List<FlowVO> flow;
	/**
	 * 流程id
	 */
	private String processId;

}
