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

/**
 * @description: 保密区供应商人员信息修改DTO
 * @date: 2020-07-30 9:29
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SecurityAreaPersonUpdateReqDTO implements Serializable {
	private static final long serialVersionUID = 6387933874447511356L;

	@ApiModelProperty(value = "记录标识",required = true)
	private Long id;

	@ApiModelProperty(value = "人员名称",required = true)
	private String personName;

	@ApiModelProperty(value = "身份证",required = true)
	private String idCard;
}
