package com.tce.smart.platform.api.dto.resp;

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
 * @description: 考勤工时明细DTO
 * @date: 2020/12/29
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class WorkDetailDTO implements Serializable {
	private static final long serialVersionUID = 203419043953813665L;

	@ApiModelProperty(value = "考勤明细")
	private List<WorkTimeDetail> workTimeDetails;

	@ApiModelProperty(value = "正班总工时")
	private Integer normalTotal;

	@Data
	public static class WorkTimeDetail{
		@ApiModelProperty(value = "工号")
		private String empNo;

		@ApiModelProperty(value = "员工姓名")
		private String empName;

		@ApiModelProperty(value = "部门名称1")
		private String dptLevel1Name;

		@ApiModelProperty(value = "部门名称2")
		private String dptLevel2Name;

		@ApiModelProperty(value = "部门名称3")
		private String dptLevel3Name;

		@ApiModelProperty(value = "出勤日期")
		@JsonFormat(pattern = "yyyy-MM-dd")
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		private Date resultDate;

		@ApiModelProperty(value = "2入时间")
		@JsonFormat(pattern = "HH:mm")
		@DateTimeFormat(pattern = "HH:mm")
		private Date inTime2;

		@ApiModelProperty(value = "2出时间")
		@JsonFormat(pattern = "HH:mm")
		@DateTimeFormat(pattern = "HH:mm")
		private Date outTime2;

		@ApiModelProperty(value = "4入时间")
		@JsonFormat(pattern = "HH:mm")
		@DateTimeFormat(pattern = "HH:mm")
		private Date inTime4;

		@ApiModelProperty(value = "4出时间")
		@JsonFormat(pattern = "HH:mm")
		@DateTimeFormat(pattern = "HH:mm")
		private Date outTime4;

		@ApiModelProperty(value = "5入时间")
		@JsonFormat(pattern = "HH:mm")
		@DateTimeFormat(pattern = "HH:mm")
		private Date inTime5;

		@ApiModelProperty(value = "5出时间")
		@JsonFormat(pattern = "HH:mm")
		@DateTimeFormat(pattern = "HH:mm")
		private Date outTime5;

		@ApiModelProperty(value = "正班时长")
		private Integer resultTotalNormalWorktime;

		@ApiModelProperty(value = "加班时长")
		private Integer resultTotalOtTime;

		@ApiModelProperty(value = "总工作时长")
		private Integer resultTotalWorktime;
	}
}
