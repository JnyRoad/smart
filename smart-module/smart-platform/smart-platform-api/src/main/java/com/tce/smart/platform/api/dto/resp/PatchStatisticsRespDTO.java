package com.tce.smart.platform.api.dto.resp;

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
public class PatchStatisticsRespDTO extends BaseDTO {
private static final long serialVersionUID = 1L;

	/**
	 * 补卡日期
	 */
	private String startTime;
    /**
   * 所属园区
   */
    private String parkName;
	/**
	 * buname
	 */
	private String compName;
	/**
	 * 部门名称
	 */
	private String depName;
	/**
	 * 补卡原因
	 */
	private String cause;
	/**
	 * 补卡原因统计数量
	 */
	private Integer statistics;


}
