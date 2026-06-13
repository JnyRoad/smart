package com.tce.smart.platform.api.dto;

import com.tce.smart.platform.api.dto.resp.EmployeeBreakOffRespDTO;
import com.tce.smart.platform.api.dto.resp.FlowRespDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 调休详情
 *
 * @author 梁圆
 * @date 2019-04-28 18:19:00
 */
@Data
public class SearchBreakoffApplicationDetailDTO implements Serializable {
	private static final long serialVersionUID = 5271472827560138128L;

	/**
	 *
	 */
	private EmployeeBreakOffRespDTO employee;

	private List<FlowRespDTO> flow;
	/**
	 * 流程id
	 */
	private String processId;
}
