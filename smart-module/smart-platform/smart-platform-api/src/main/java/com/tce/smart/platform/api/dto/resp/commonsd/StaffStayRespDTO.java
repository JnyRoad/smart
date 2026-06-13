package com.tce.smart.platform.api.dto.resp.commonsd;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: 员工入住信息
 * @date: 2020/10/12 17:32
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StaffStayRespDTO implements Serializable {
	private static final long serialVersionUID = -856830185573122380L;

	@ApiModelProperty(value = "员工工号")
	private String staffBadge;

	@ApiModelProperty(value = "员工姓名")
	private String  staffName;

	@ApiModelProperty(value = "抄表月份")
	@JsonFormat(pattern = "yyyy-MM")
	@DateTimeFormat(pattern = "yyyy-MM")
	private Date meterMonth;

	@ApiModelProperty(value = "入住时间")
	private Date inTime;

	@ApiModelProperty(value = "入住天数")
	private Integer tayDays;

	@ApiModelProperty(value = "结算时间")
	private Date statementDate;

	@ApiModelProperty(value = "备注")
	private String remark;
}
