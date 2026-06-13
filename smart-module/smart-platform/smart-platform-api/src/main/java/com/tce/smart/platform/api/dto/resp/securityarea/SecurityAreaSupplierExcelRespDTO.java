package com.tce.smart.platform.api.dto.resp.securityarea;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.*;
import java.io.Serializable;

/**
 * @description: 保密区供应商表
 * @date: 2020-07-20 13:37
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SecurityAreaSupplierExcelRespDTO implements Serializable {

	private static final long serialVersionUID = 9181621159534800693L;

	@Excel(name = "供应商名称", isImportField = "true_st")
	private String companyName;

	@Excel(name = "协议签订日期", isImportField = "true_st")
	private String beginEffectTime;

	@Excel(name = "协议到期日期", isImportField = "true_st")
	private String endEffectTime;

	@Excel(name = "申请理由", isImportField = "true_st")
	private String remark;

	@Excel(name = "联系人", isImportField = "true_st")
	private String contactPerson;

	@Excel(name = "授权区域", isImportField = "true_st")
	private String authorizedArea;

	@Excel(name = "授权人数", isImportField = "true_st")
	private Integer authPersonNum;
}
