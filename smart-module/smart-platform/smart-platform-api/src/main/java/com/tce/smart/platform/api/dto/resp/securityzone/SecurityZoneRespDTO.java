package com.tce.smart.platform.api.dto.resp.securityzone;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 *保密区维护
 * @author fushiping
 * @date 2021-07-29 11:12:46
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecurityZoneRespDTO implements Serializable {
private static final long serialVersionUID = 1L;

	@JsonFormat(shape= JsonFormat.Shape.STRING)
	@ApiModelProperty("ID")
	private Long id;

	@ApiModelProperty("保密区名")
    private String securityName;

	@ApiModelProperty("保密区code")
    private String securityCode;

	@ApiModelProperty("园区id")
    private Integer parkId;

	@ApiModelProperty("园区名")
	private String parkName;

	@ApiModelProperty("创建时间")
    private LocalDateTime createTime;

	@ApiModelProperty("修改时间")
    private LocalDateTime updateTime;

	@ApiModelProperty("项目权限名")
	private List<String> authNameList;

	@ApiModelProperty("项目权限")
	private List<AuthList> authLists;

	@Data
	public static class AuthList {

		@ApiModelProperty("权限id")
		private Integer authId;

		@ApiModelProperty("权限名")
		private String authName;
	}

}
