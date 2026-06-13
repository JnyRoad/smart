package com.tce.smart.platform.api.dto.req.leavecount;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 *
 * @author fushiping
 * @date 2022-06-21 11:02:12
 */
@Data
public class SettlementInfoQueryReqDTO extends BaseDTO {


	@ApiModelProperty("园区id")
    private Integer parkId;

	@ApiModelProperty("工号")
    private String badge;

	@ApiModelProperty("姓名")
    private String name;

	@ApiModelProperty("BU")
    private String bu;

	@ApiModelProperty("开始离职时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime startLeaveDate;

	@ApiModelProperty("结束离职时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime endLeaveDate;

	@ApiModelProperty("结算状态")
    private Integer status;

	@ApiModelProperty("开始结算时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

	@ApiModelProperty("结束结算时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime endTime;

}
