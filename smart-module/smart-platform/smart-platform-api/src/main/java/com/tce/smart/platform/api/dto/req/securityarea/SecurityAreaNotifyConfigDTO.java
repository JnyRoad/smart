package com.tce.smart.platform.api.dto.req.securityarea;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @description: 保密区通知配置DTO
 * @date: 2020-07-30 9:29
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SecurityAreaNotifyConfigDTO implements Serializable {
	private static final long serialVersionUID = -7173359971172922068L;

	@ApiModelProperty(value = "园区id",required = true)
	private Integer parkId;

	@ApiModelProperty(value = "天数",required = true)
	private Integer days;

	@ApiModelProperty(value = "通知类型 1.邮件",required = true)
	private Integer notifyType;

	@ApiModelProperty(value = "通知模板",required = true)
	private String template;

	@ApiModelProperty(value = "通知名单",required = true)
	private List<String> accounts;
}
