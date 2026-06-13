package com.tce.smart.platform.wrapper.securityzone;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.securityzone.TaskDetailsRespDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityTaskDetails;
import com.tce.smart.tool.enums.DeviceDownStatusEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: OaAreaRelationWrapper
 * @Author fushiping
 * @Date
 */
@Component
@AllArgsConstructor
public class SecurityTaskDetailWrapper extends BaseWrapper<SmtSecurityTaskDetails, TaskDetailsRespDTO> {

	@Override
	protected TaskDetailsRespDTO warp(SmtSecurityTaskDetails bean) throws IOException {
		TaskDetailsRespDTO resp = BeanUtils.transform(TaskDetailsRespDTO.class, bean);
		resp.setStatusDesc(DeviceDownStatusEnum.desc(bean.getStatus()));
		resp.setAreaName(bean.getAreaName());
		resp.setAuth(bean.getAuthName());
		return resp;
	}
}
