package com.tce.smart.platform.api.dto.resp.admittance;

import lombok.Data;

import java.io.Serializable;

@Data
public class VisitorApprovalNodeRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String title;

	private String state;

	private String statusText;

	private String approverName;

	private String time;

	private String comment;
}
