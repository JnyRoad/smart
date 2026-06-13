package com.tce.smart.platform.wrapper.securityzone;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.securityzone.OaFlowRespDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityAuthApplyDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityConfigParkListRespDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthApply;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthDelete;
import com.tce.smart.platform.core.vo.FlowVO;
import com.tce.smart.platform.service.SmtOutDormitoryStaffService;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.securityzone.SmtSecurityTaskDetailsService;
import com.tce.smart.tool.enums.ApproveListStateEnum;
import com.tce.smart.tool.enums.DeviceDownStatusEnum;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: OaAreaRelationWrapper
 * @Author fushiping
 * @Date
 */
@Component
@AllArgsConstructor
public class SecurityConfigListWrapper extends BaseWrapper<SmtSecurityAuthDelete, SecurityConfigParkListRespDTO> {

	@Autowired
	private SmtParkService smtParkService;

	@Override
	protected SecurityConfigParkListRespDTO warp(SmtSecurityAuthDelete bean) throws IOException {
		SecurityConfigParkListRespDTO resp = BeanUtils.transform(SecurityConfigParkListRespDTO.class, bean);
		if(Objects.nonNull(bean.getParkId())) {
			resp.setParkName(smtParkService.getById(bean.getParkId()).getParkName());
		}
		return resp;
	}
}
