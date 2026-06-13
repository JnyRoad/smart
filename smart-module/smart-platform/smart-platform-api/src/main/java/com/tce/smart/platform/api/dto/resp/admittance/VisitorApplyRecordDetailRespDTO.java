package com.tce.smart.platform.api.dto.resp.admittance;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class VisitorApplyRecordDetailRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String applyId;

	private String applyNo;

	private String parkName;

	private String applyStatus;

	private String dispatchStatus;

	private String receptionistName;

	private String startTime;

	private String endTime;

	private String cause;

	private String visitorName;

	private String visitorPhone;

	private List<VisitorApplyFellowRespDTO> fellows = new ArrayList<>();

	private List<VisitorApplyVehicleRespDTO> vehicles = new ArrayList<>();

	private List<String> areas = new ArrayList<>();

	private String submitTime;
}
