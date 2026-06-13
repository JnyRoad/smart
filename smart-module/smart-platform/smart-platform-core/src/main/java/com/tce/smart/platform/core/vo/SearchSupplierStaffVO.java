package com.tce.smart.platform.core.vo;

import java.util.Date;

import lombok.Data;

/**
 * 查看园区供应商员工
 * @author QIPEI
 *
 */
@Data
public class SearchSupplierStaffVO {


	private Integer id;


	private String name;

	private Integer supplierId;

	private String remark;

	private String phone;

	private Date createTime;

	private Integer parkId;

	private String parkName;

	private String SupplierName;
}
