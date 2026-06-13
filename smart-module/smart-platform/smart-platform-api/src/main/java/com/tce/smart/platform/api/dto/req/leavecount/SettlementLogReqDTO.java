package com.tce.smart.platform.api.dto.req.leavecount;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import zipkin2.Call;

import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:02:04
 */
@Data
public class SettlementLogReqDTO extends BaseDTO {
private static final long serialVersionUID = 1L;

	@ApiModelProperty("调用事务id")
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
    private LocalDateTime responseLog;

	@ApiModelProperty("响应报文")
    private String responseTime;

}
