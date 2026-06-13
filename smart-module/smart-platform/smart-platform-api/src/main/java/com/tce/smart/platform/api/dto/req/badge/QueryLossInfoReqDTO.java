package com.tce.smart.platform.api.dto.req.badge;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 厂牌挂失记录
 *
 * @author fushiping
 * @date 2020-07-07 11:47:43
 */
@Data
public class QueryLossInfoReqDTO extends BaseDTO {

    /**
   * 员工工号
   */
	@ApiModelProperty("员工工号")
    private String badge;
    /**
   * 员工姓名
   */
	@ApiModelProperty("员工姓名")
    private String name;
	/**
	 * BUId
	 */
	@ApiModelProperty("BUId")
	private String compId;
	/**
	 * 部门ID
	 */
	@ApiModelProperty("部门ID")
	private String depId;
    /**
   * 园区名
   */
	@ApiModelProperty("园区名")
    private Integer parkId;
	/**
	 * 开始时间
	 */
	@ApiModelProperty("开始时间")
    private LocalDateTime startTime;
	/**
	 * 截止时间
	 */
	@ApiModelProperty("截止时间")
    private LocalDateTime endTime;

}
