package com.tce.smart.platform.api.dto.resp.admittance;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class VisitorSelfQueryRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String queryToken;

	private String maskedName;

	private String maskedMobile;

	private List<VisitorApplyRecordRespDTO> records = new ArrayList<>();
}
