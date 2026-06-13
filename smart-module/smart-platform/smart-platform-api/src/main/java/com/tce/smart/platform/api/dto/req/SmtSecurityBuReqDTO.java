package com.tce.smart.platform.api.dto.req;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 园区BU关系表
 *
 * @author mckaywu
 * @date 2019-11-20 10:35:16
 */
@Data
public class SmtSecurityBuReqDTO extends BaseDTO {

    /**
   * 园区编号
   */
	@ApiModelProperty(value = "园区编号")
    private Integer parkId;
    /**
   * BU编号
   */
	@ApiModelProperty(value = "BU编号")
    private String compId;

	/**
	 * 权限策略id
	 */
	@ApiModelProperty(value = "权限策略id")
	private List<Integer> securityId;

}
