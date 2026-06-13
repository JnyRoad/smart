package com.tce.smart.platform.api.dto.req.sddto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: 查询公摊水电抄表DTO
 * @date: 2020/10/12 0012 17:46
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SearchCommonSDMeterreadReqDTO implements Serializable {
	private static final long serialVersionUID = 793131149625473307L;

	@ApiModelProperty(value = "公摊水电表记录ID")
	private Long id;

	@ApiModelProperty(value = "抄表月份")
	@JsonFormat(pattern = "yyyy-MM")
	@DateTimeFormat(pattern = "yyyy-MM")
	private Date meterMonth;

	@ApiModelProperty(value = "收费项目 1.热水 2.冷水 3.电")
	private Integer categoryId;

	@ApiModelProperty(value = "当前页")
	private Long current;

	@ApiModelProperty(value = "页大小")
	private Long size;

	@ApiModelProperty(value = "园区ID")
	private Integer parkId;

	@ApiModelProperty(value = "楼栋ID")
	private Integer dormitoryId;
}
