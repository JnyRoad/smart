package com.tce.smart.platform.api.dto.resp.dormitorymange;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description: 条件查询房间结果DTO
 * @date: 2020/9/29 8:48
 * @author: fushiping
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DormitoryLockInfoRespDTO implements Serializable {
	private static final long serialVersionUID = 1470561687953010349L;

	@ApiModelProperty(value = "是否录入指纹")
	private Integer fingerprintCode;

	@ApiModelProperty(value = "是否存在动态码")
	private Integer dynamicCode;

	@ApiModelProperty(value = "是否录入指纹")
	private String fingerprintDesc;

	@ApiModelProperty(value = "动态码")
	private String dynamicDesc;

}
