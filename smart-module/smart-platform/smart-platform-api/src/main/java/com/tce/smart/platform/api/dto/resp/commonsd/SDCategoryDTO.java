package com.tce.smart.platform.api.dto.resp.commonsd;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 水电项目DTO
 * @date: 2020/10/12 17:41
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SDCategoryDTO {

	@ApiModelProperty(value = "抄表记录ID")
	private Long mrId;

	@ApiModelProperty(value = "收费项目 1.热水 2.冷水 3.电")
	private Integer categoryId;

	@ApiModelProperty(value = "上月止度")
	private Double preMonthNum;

	@ApiModelProperty(value = "本月止度")
	private Double curMonthNum;

	@ApiModelProperty(value = "上月止度是否修正 0.未修正 1.已修正")
	private Integer isRevise;
}
