package com.tce.smart.app.vo.fore;

import com.tce.smart.common.core.vo.BaseVO;
import com.tce.smart.platform.api.dto.resp.EmployeeOverTimeRespDTO;
import com.tce.smart.platform.api.dto.resp.FlowRespDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 加班详情信息VO
 *
 * @author ly
 * @date 2019-05-10 16:11:13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExtraWorkDetailVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 5362758608679031031L;

	/**
	 * 加班的员工信息
	 */
	private EmployeeOverTimeRespDTO employee;

	/**
	 * 流程节点
	 */
	private List<FlowRespDTO> flow;

	private String processId;

}
