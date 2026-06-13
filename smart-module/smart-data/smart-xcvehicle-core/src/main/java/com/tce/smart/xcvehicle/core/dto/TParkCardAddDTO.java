package com.tce.smart.xcvehicle.core.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class TParkCardAddDTO implements Serializable {
	private static final long serialVersionUID = -1;

    private String userName;

	private String remark;

	private String phone;

	private String plat;

	private Integer ctId;

	private Integer fctCode;

	private Integer carColor;

	private Integer carType;

	private String startDate;

	private String endDate;

	private String cUser;

	private String hCardNo;
}
