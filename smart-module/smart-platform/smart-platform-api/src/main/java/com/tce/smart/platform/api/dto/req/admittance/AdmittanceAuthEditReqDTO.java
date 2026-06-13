package com.tce.smart.platform.api.dto.req.admittance;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.omg.CORBA.INTERNAL;

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
public class AdmittanceAuthEditReqDTO implements Serializable {
private static final long serialVersionUID = 1L;

	@ApiModelProperty("id")
    private Long id;

	@ApiModelProperty("oa区域类型code")
	private Integer areaTypeId;

	@ApiModelProperty("关联权限策略")
	private List<AuthList> authLists;

	@ApiModelProperty("园区ID")
	private Integer parkId;

	@Data
	public static class AuthList{

		@ApiModelProperty("权限策略名")
		private String authName;

		@ApiModelProperty("权限策略id")
		private Integer authId;

		@ApiModelProperty("权限类型")
		private Integer authType;
	}

}
