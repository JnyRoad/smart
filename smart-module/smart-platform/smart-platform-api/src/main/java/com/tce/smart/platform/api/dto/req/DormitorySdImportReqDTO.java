package com.tce.smart.platform.api.dto.req;

import cn.afterturn.easypoi.excel.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @program: smart-module
 * @description:
 * @author: Wuling
 * @create: 2021-07-15 09:02
 **/
@Data
public class DormitorySdImportReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "楼栋名称")
	@Excel(name = "楼栋名称", isImportField = "dorName")
	private String dorName;

	@ApiModelProperty(value = "楼层名称")
	@Excel(name = "楼层名称", isImportField = "floorName")
	private String floorName;

	@ApiModelProperty(value = "房间名称")
	@Excel(name = "房间名称", isImportField = "roomName")
	private String roomName;

	@ApiModelProperty(value = "电上月止数")
	@Excel(name = "电上月止数", isImportField = "elePreNum")
	private String elePreNum;

	@ApiModelProperty(value = "电本月止数")
	@Excel(name = "电本月止数", isImportField = "eleCurNum")
	private String eleCurNum;

	@ApiModelProperty(value = "冷水上月止数")
	@Excel(name = "冷水上月止数", isImportField = "coldPreNum")
	private String coldPreNum;

	@ApiModelProperty(value = "冷水本月止数")
	@Excel(name = "冷水本月止数", isImportField = "coldCurNum")
	private String coldCurNum;

	@ApiModelProperty(value = "热水上月止数")
	@Excel(name = "热水上月止数", isImportField = "hotPreNum")
	private String hotPreNum;

	@ApiModelProperty(value = "热水本月止数")
	@Excel(name = "热水本月止数", isImportField = "hotCurNum")
	private String hotCurNum;

	@Excel(name = "备注", isImportField = "remark")
	private String remark;
}
