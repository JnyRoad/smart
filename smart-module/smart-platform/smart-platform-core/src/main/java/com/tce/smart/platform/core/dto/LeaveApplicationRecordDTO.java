package com.tce.smart.platform.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * 离职申请
 * @author Lenovo
 *
 */
@Data
public class LeaveApplicationRecordDTO {

	/**
	 * 员工号
	 */
	private String badge;

	/**
	 * 姓名
	 */
	private String name;

	/**
	 * 离职开始时间
	 */
	private String startTime;

	/**
	 * 离职结束时间
	 */
	private String endTime;

	private List<Integer> parkIds;
}
