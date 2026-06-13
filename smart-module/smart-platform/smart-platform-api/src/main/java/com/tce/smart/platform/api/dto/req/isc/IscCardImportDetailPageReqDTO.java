package com.tce.smart.platform.api.dto.req.isc;

import lombok.Data;

import java.util.List;

@Data
public class IscCardImportDetailPageReqDTO {

	private Long batchId;

	private List<Integer> parkIds;

	private String badge;

	private String name;

	private String iscCardNo;

	private String resultCode;
}
