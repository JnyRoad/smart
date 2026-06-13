package com.tce.smart.platform.core.dto;


import java.time.LocalDateTime;

import lombok.Data;

/**
 * 查询园区供应商员工
 * @author QIPEI
 *
 */
@Data
public class SearchSupplierStaffDTO {

	private String name;

	private String supplierId;

	private String remark;

	private String phone;

	private LocalDateTime createTime;

}
