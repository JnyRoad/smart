package com.tce.smart.platform.core.vo;

import java.util.List;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 职工出差申请详情
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchTravelApplicationDetailVO extends Model<SearchTravelApplicationDetailVO> {
	private static final long serialVersionUID = 1L;

	/**
	 * 出差详情
	 */
	private CcdFormtableMainVO employee;

	/**
	 * 日程数据
	 */
	private CcdFormtableMainDt1VO employeeDay;

	/**
	 * 报告数据
	 */
	private CcdFormtableMainDt1VO employeeReport;

	/**
	 * 审批数据
	 */
	private List<FlowVO> flow;
}
