package com.tce.smart.platform.api.dto.resp.admittance;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-08-17 17:45:23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdmittanceAuthTypeRespDTO implements Serializable {
private static final long serialVersionUID = 1L;

	@ApiModelProperty("oa区域类型id")
	private Long areaTypeId;

	@ApiModelProperty("oa区域类型名")
	private String areaTypeName;

	@ApiModelProperty("oa区域ID")
	private String areaOaId;

}
