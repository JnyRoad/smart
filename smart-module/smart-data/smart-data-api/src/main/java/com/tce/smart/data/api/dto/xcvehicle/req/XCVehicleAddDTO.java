package com.tce.smart.data.api.dto.xcvehicle.req;

import lombok.Data;

import java.io.Serializable;

@Data
public class XCVehicleAddDTO implements Serializable {
	private static final long serialVersionUID = -1;

    private String userName;

	private String badge;

	private String phone;

	private String plat;

	private Integer fctCode;

	private Integer ctId;

	private Integer carColor;

	private String startDate;

	private String endDate;

	private String cUser;
}
