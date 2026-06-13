package com.tce.smart.platform.api.dto.req.dormitorymange;

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
 * @description: 内宿申请记录查询DTO
 * @date: 2020/12/29
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DormitoryApplySearchReqDTO implements Serializable {
	private static final long serialVersionUID = 3382879825360968352L;

	@ApiModelProperty(value = "园区Id")
	private Integer parkId;

	@ApiModelProperty(value = "员工工号")
	private String staffBadge ;

	@ApiModelProperty(value = "员工姓名")
	private String staffName;

	@ApiModelProperty(value = "申请时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date applyDate;

	@ApiModelProperty(value = "回复时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date resultDate;

	@ApiModelProperty(value = "状态 1.申请中 2.申请通过 3.退回 4.撤销")
	private Integer status;

	@ApiModelProperty(value = "当前页")
	private Long current;

	@ApiModelProperty(value = "页大小")
	private Long size;
}
