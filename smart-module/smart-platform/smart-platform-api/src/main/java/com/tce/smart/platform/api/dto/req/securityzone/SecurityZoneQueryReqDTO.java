package com.tce.smart.platform.api.dto.req.securityzone;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

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
public class SecurityZoneQueryReqDTO implements Serializable {
private static final long serialVersionUID = 1L;

	@ApiModelProperty("ID集合")
	private List<Long> id;

	@ApiModelProperty("保密区名")
    private String securityName;

	@ApiModelProperty("保密区code")
    private String securityCode;

	@ApiModelProperty("园区id")
    private Integer parkId;

	@ApiModelProperty("创建时间")
    private LocalDateTime createTime;

}
