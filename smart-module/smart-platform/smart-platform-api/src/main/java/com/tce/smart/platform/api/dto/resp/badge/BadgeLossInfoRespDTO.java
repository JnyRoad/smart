package com.tce.smart.platform.api.dto.resp.badge;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 厂牌挂失
 *
 * @author fushiping
 * @date 2020-07-07 11:47:43
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BadgeLossInfoRespDTO extends BaseDTO {

    /**
   * 主键
   */
	@ApiModelProperty("主键")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;
    /**
   * 员工工号
   */
	@ApiModelProperty("员工工号")
    private String badge;
    /**
   * 员工姓名
   */
	@ApiModelProperty("员工姓名")
    private String name;
    /**
   * BU名
   */
	@ApiModelProperty("BU名")
    private String compName;
    /**
   * 部门名
   */
	@ApiModelProperty("部门名")
    private String depName;
    /**
   * 园区名
   */
	@ApiModelProperty("园区名")
    private String parkName;
    /**
   * 挂失时间
   */
	@ApiModelProperty("挂失时间")
    private LocalDateTime createTime;


}
