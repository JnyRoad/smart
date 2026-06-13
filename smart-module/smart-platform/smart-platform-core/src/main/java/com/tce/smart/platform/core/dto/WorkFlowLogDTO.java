package com.tce.smart.platform.core.dto;

import com.tce.smart.tool.constant.WorkFlowLogConstants;

import lombok.Data;

import java.util.List;
import java.util.Objects;

/**
 *
 * @ClassName WorkFlowLogDTO.java
 * @Author mingkai.wu
 * @Date 2019-04-29 09:05
 * @Description 接收OA审核信息
 */
@Data
public class WorkFlowLogDTO {

	/**
	 * 类型
	 */
	private Integer type;

	/**
	 * 错误码
	 */
	private Integer errorcode;

	/**
	 * 响应消息
	 */
	private String message;

	/**
	 * 流程
	 */
	private List<WorkFlowLogDataDTO> resultdata;

	public boolean success(){
		return Objects.nonNull(this.type) && this.type.equals(WorkFlowLogConstants.SUCCESS);
	}
}
