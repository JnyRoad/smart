package com.tce.smart.platform.api.dto.resp.securityzone;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 *
 *权限删除白名单
 * @author fushiping
 * @date 2021-07-29 11:13:07
 */
@Data
public class SecurityWhiteRespDTO implements Serializable {

private static final long serialVersionUID = 1L;

	@ApiModelProperty("ID集合")
	@JsonFormat(shape=JsonFormat.Shape.STRING)
	private Long id;

	@ApiModelProperty("员工id")
	@JsonFormat(shape=JsonFormat.Shape.STRING)
    private Long staffId;

	@ApiModelProperty("配置id")
	@JsonFormat(shape=JsonFormat.Shape.STRING)
    private Long deleteConfigId;

	@ApiModelProperty("员工工号")
    private String staffBadge;

	@ApiModelProperty("员工姓名")
    private String staffName;


}
