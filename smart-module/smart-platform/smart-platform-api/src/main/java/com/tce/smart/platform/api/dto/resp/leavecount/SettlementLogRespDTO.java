package com.tce.smart.platform.api.dto.resp.leavecount;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:02:04
 */
@Data
public class SettlementLogRespDTO extends BaseDTO {
private static final long serialVersionUID = 1L;

	@ApiModelProperty("调用事务id")
	@JsonSerialize(using = ToStringSerializer.class)
    private Long infoId;

	@ApiModelProperty("调用方名称")
    private String requestName;

	@ApiModelProperty("调用方IP")
    private String requestIp;

	@ApiModelProperty("调用报文")
    private String requestLog;

	@ApiModelProperty("调用时间")
    private LocalDateTime requestTime;

	@ApiModelProperty("响应状态")
    private String responseStatus;

	@ApiModelProperty("响应描述")
    private String responseDesc;

	@ApiModelProperty("响应时间")
    private LocalDateTime responseTime;

	@ApiModelProperty("响应报文")
    private String responseLog;

}
