package com.tce.smart.platform.api.dto.req.leavecount;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import zipkin2.Call;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:01:56
 */
@Data
public class SettlementTemplateReqDTO extends BaseDTO {
private static final long serialVersionUID = 1L;

	@JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty("ID")
    private Long id;

	@ApiModelProperty("模板名称")
    private String templateName;

	@ApiModelProperty("所属园区id")
    private Integer parkId;

}
