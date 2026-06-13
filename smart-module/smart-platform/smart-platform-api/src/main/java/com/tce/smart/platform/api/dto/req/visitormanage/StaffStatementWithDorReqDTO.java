package com.tce.smart.platform.api.dto.req.visitormanage;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @description: SmtStaffStatementReqDTO
 * @date: 2020-07-17 17:59
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StaffStatementWithDorReqDTO implements Serializable {
	private static final long serialVersionUID = -7289022427725195102L;

	@ApiModelProperty("楼栋Id列表")
	private String dormitoryIds;

	@ApiModelProperty(value = "抄表月份",required = true)
	@JsonFormat(pattern="yyyy-MM")
	@DateTimeFormat(pattern="yyyy-MM")
	private Date meterMonth;
}
