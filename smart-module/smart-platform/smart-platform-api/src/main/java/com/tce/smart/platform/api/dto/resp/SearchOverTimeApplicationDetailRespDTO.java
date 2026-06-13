package com.tce.smart.platform.api.dto.resp;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 加班详情
 *
 * @author 梁圆
 * @date 2019-04-28 18:19:00
 */
@Data
public class SearchOverTimeApplicationDetailRespDTO implements Serializable {
	private static final long serialVersionUID = -6899368071500699226L;

	/**
	 *
	 */
	private EmployeeOverTimeRespDTO employee;

	private List<FlowRespDTO> flow;

	private String processId;
}
