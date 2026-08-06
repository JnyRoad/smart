package com.tce.smart.platform.api.dto.resp.admittance;

import lombok.Data;

import java.io.Serializable;

/** 访客申请流程所需的最小接待人投影。 */
@Data
public class VisitorReceptionistRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String receptionistBadge;
	private String receptionistName;
	private String receptionistPhone;
}
