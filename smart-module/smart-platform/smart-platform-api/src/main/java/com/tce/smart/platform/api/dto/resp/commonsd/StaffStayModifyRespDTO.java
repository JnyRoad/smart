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
 * @description: 员工入住天数修改DTO
 * @date: 2020/10/12 17:32
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StaffStayModifyRespDTO implements Serializable {
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

	@ApiModelProperty(value = "修改前天数")
	private Integer oldDays;

	@ApiModelProperty(value = "修改后天数")
	private Integer newDays;

	@ApiModelProperty(value = "修改人")
	private String meterName;

	@ApiModelProperty(value = "备注")
	private String remark;

	@ApiModelProperty(value = "修改时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

}
