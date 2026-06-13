package com.tce.smart.platform.api.dto.resp.admittance;

import lombok.Data;

import java.io.Serializable;

@Data
public class VisitorApplyVehicleRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String plate;

	private String type;
}
