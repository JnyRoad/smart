package com.tce.smart.platform.api.dto.resp;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;

import java.util.Date;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-28 15:37
 */
@Data
public class ArticlesReleaseExcelRespDTO extends BaseDTO {
	@Excel(name = "所属园区")
	private String parkName;
	@Excel(name = "申请人员")
	private String name;
	@Excel(name = "物品类型")
	private String articlesTypeName;
	@Excel(name = "物品描述")
	private String articlesDesc;
	@Excel(name = "携带人")
	private String carrier;
	@Excel(name = "计划离园时间")
	private Date plannedDepartureTime;
	@Excel(name = "车牌号")
	private String licensePlate;
	@Excel(name = "备注")
	private String remarks;
	@Excel(name = "状态")
	private String statusName;
	@Excel(name = "审批人员")
	private String approver;
	@Excel(name = "审批时间")
	private Date approveTime;
	@Excel(name = "保安人员")
	private String securityStaff;
	@Excel(name = "离园时间")
	private Date departureTime;
}
