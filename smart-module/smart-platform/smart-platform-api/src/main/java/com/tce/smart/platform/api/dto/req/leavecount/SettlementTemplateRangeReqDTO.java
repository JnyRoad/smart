package com.tce.smart.platform.api.dto.req.leavecount;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:01:50
 */
@Data
public class SettlementTemplateRangeReqDTO extends BaseDTO {
private static final long serialVersionUID = 1L;


	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty("ID")
    private Long id;

	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty("模板ID")
    private Long tempId;

	@ApiModelProperty("范围类型 1 房间 2 bu")
    private Integer type;

	@ApiModelProperty("值")
    private String value;

	@ApiModelProperty("园区id")
    private Integer parkId;

}
