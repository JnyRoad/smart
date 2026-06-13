package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Li.JiaJun
 * @since 2022/7/21 14:41
 */
@Data
public class OperateLogRespDTO extends BaseDTO {

	@ApiModelProperty("功能类型")
	private Integer code;

	@ApiModelProperty("功能描述")
	private String desc;

	@ApiModelProperty("操作动作")
	private Integer action;

	@ApiModelProperty("操作动作描述")
	private String actionDesc;

	@ApiModelProperty("操作人")
	private String createUserName;

	@ApiModelProperty("操作用户ID")
	private Integer createUserId;

	@ApiModelProperty("操作时间")
	private LocalDateTime createTime;
}
