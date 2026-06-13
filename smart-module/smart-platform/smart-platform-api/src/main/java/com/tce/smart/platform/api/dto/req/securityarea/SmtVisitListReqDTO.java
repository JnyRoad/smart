package com.tce.smart.platform.api.dto.req.securityarea;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description: SmtVisitListReqDTO
 * @date: 2020-07-30 9:31
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SmtVisitListReqDTO implements Serializable {
	private static final long serialVersionUID = 4753850672234195362L;

	/**
	 * 姓名
	 */
	@ApiModelProperty(value = "姓名")
	private String visitName;

	/**
	 * 手机号
	 */
	@ApiModelProperty(value = "手机号")
	private String phone;

	/**
	 * 身份证
	 */
	@ApiModelProperty(value = "身份证")
	private String visitCardId;

}
