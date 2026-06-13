package com.tce.smart.platform.api.dto.req;

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
public class PatchStatisticsReqDTO extends BaseDTO {
private static final long serialVersionUID = 1L;
    /**
   * 所属园区
   */
    private Integer parkId;

	/**
	 * buname
	 */
	private String compId;
	/**
	 * 部门名称
	 */
	private String depId;
	/**
	 * 考核月份
	 */
	private String startTime;


}
