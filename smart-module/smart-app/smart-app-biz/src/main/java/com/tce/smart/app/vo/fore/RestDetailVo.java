package com.tce.smart.app.vo.fore;

import com.tce.smart.common.core.vo.BaseVO;
import com.tce.smart.platform.api.dto.resp.FlowRespDTO;
import lombok.Data;

import java.util.List;

/**
 * 调休详情信息VO
 *
 * @author ly
 * @date 2019-05-10 16:11:13
 */
@Data
public class RestDetailVo extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 5362758608679031031L;

	/**
	 * 调休的员工信息
	 */
	private EmployeeRestDetailVo employee;

	/**
	 * 流程节点
	 */
	private List<FlowRespDTO> flow;

	/**
	 * 流程id
	 */
	private String processId;

}
