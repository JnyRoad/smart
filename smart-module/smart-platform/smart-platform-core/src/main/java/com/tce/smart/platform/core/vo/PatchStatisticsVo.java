package com.tce.smart.platform.core.vo;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2020-07-17 16:51:38
 */
@Data
public class PatchStatisticsVo extends BaseDTO {
private static final long serialVersionUID = 1L;

	/**
	 * 所属园区
	 */
	private Integer parkId;
	/**
	 * buname
	 */
	private String compName;
	/**
	 * 部门名称
	 */
	private String depName;
	/**
	 * 考核月份
	 */
	private String startTime;
	/**
	 * 补卡原因
	 */
	private Integer cause;
	/**
	 * 补卡原因统计数量
	 */
	private Integer statistics;


}
