package com.tce.smart.platform.api.dto.resp.leavecount;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:01:40
 */
@Data
public class SettlementTemplateJcheRespDTO extends BaseDTO {
private static final long serialVersionUID = 1L;

    /**
   * 主键ID
   */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty("主键ID")
    private Long id;
    /**
   * 级层id
   */
	@ApiModelProperty("级层id")
    private String jcheId;
    /**
   * 级层名
   */
	@ApiModelProperty("级层名")
    private String jcheName;

}
