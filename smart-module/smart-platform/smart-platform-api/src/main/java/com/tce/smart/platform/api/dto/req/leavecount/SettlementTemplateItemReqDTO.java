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

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:01:40
 */
@Data
public class SettlementTemplateItemReqDTO extends BaseDTO {
private static final long serialVersionUID = 1L;

	@ApiModelProperty("水电规则")
	private List<SettlementTemplateRuleReqDTO> rules;

	@ApiModelProperty("适用级层")
	private List<SettlementTemplateJcheReqDTO> jches;

}
