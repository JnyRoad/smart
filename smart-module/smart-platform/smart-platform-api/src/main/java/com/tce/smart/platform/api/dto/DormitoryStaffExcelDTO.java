package com.tce.smart.platform.api.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.util.Date;

/**
 *
 * @author sunfujian
 * @date 2021/5/20 9:13
 */
@Data
public class DormitoryStaffExcelDTO {
	@Excel(name = "序号", isImportField = "bedNum")
	private Integer bedNum;

	@Excel(name = "工号", isImportField = "badge")
	private String badge;

	@Excel(name = "姓名", isImportField = "staffName")
	private String staffName;

	@Excel(name = "部门", isImportField = "dept")
	private String dept;

	@Excel(name = "职务", isImportField = "jobName")
	private String jobName;

	@Excel(name = "原因", isImportField = "reason")
	private String reason;

	@Excel(name = "入住日期", isImportField = "inTime")
	private String inTime;

	@Excel(name = "退宿日期", isImportField = "outTime")
	private String outTime;

	@Excel(name = "房间", isImportField = "roomName")
	private String roomName;

	@Excel(name = "已住人数", isImportField = "usedBed")
	private Integer usedBed;

	@Excel(name = "空床位", isImportField = "leftBed")
	private Integer leftBed;

	@Excel(name = "S4", isImportField = "four")
	private String four;

	@Excel(name = "集团", isImportField = "company")
	private String company;

	@Excel(name = "备注", isImportField = "remark")
	private String remark;

	@Excel(name = "失败原因", isImportField = "respRemark")
	private String respRemark;
}
