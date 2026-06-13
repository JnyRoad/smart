package com.tce.smart.platform.core.vo;


import lombok.Data;

@Data
public class CallowanceCancelInfoVO {
	private Integer id;

	private String name;

	private String processResult;

	private String createTime;

	private String backDate;

	private String allowanceTypeName;

	private Integer allowanceType;
}
