package com.tce.smart.platform.api.dto.resp.admittance;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class VisitorApplyRecordRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String applyId;

	private String parkName;

	private String applyStatus;

	private String receptionistName;

	private String startTime;

	private String endTime;

	private Integer fellowCount;

	private List<String> plates = new ArrayList<>();

	private String currentNode;

	private String dispatchStatus;

	private String submitTime;
}
