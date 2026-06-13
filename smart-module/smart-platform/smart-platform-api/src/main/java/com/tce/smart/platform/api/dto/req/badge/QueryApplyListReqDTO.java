package com.tce.smart.platform.api.dto.req.badge;

import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 厂牌补领申请记录
 *
 * @author fushiping
 * @date 2020-07-07 11:47:43
 */
@Data
public class QueryApplyListReqDTO extends BaseDTO {

    /**
   * 员工工号
   */
    private String badge;
    /**
   * 员工姓名
   */
    private String name;

	/**
	 * BUId
	 */
	private String compId;
	/**
	 * 部门ID
	 */
	private String depId;
    /**
   * 园区名
   */
    private Integer parkId;
	/**
	 * 开始时间
	 */
	private LocalDateTime startTime;
	/**
	 * 截止时间
	 */
	private LocalDateTime endTime;
	/**
	 * 处理状态
	 */
	private Integer state;

}
