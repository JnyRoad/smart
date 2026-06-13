package com.tce.smart.platform.api.dto.req.leavecount;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:01:40
 */
@Data
public class SettlementCountReqDTO extends BaseDTO {
private static final long serialVersionUID = 1L;

	@ApiModelProperty("离职日期")
    private String leaveDate;

	@ApiModelProperty("工号")
	private String badge;

	@ApiModelProperty("密匙")
	private String token;

}
