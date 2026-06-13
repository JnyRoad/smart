package com.tce.smart.data.wrapper.oa;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.businesstrip.core.entity.VWorkflowSelectitem;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.oa.resp.WorkflowSelectitemRespDTO;
import org.springframework.stereotype.Component;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName:
 * @Author
 * @Date
 */
@Component
public class WorkflowSelectitemWrapper extends BaseWrapper<VWorkflowSelectitem, WorkflowSelectitemRespDTO> {

	@Override
	protected WorkflowSelectitemRespDTO warp(VWorkflowSelectitem selectitem) {
		WorkflowSelectitemRespDTO workflow = new WorkflowSelectitemRespDTO();
		BeanUtil.copyProperties(selectitem, workflow);
		return workflow;
	}
}
