package com.tce.smart.platform.wrapper;

import com.tce.smart.admin.api.dto.RoleDTO;
import com.tce.smart.admin.api.dto.UserInfo;
import com.tce.smart.admin.api.feign.RemoteUserService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.OrganizeRelationRespDTO;
import com.tce.smart.platform.core.entity.SmtOrganizeRelation;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.service.SmtOrganizeAccessService;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.tool.enums.OrganizeSourceEnum;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 */
@Component
@AllArgsConstructor
public class OrganizeRelationDetailWrapper extends BaseWrapper<SmtOrganizeRelation, OrganizeRelationRespDTO> {
	@Autowired
	private SmtParkService parkService;
	@Autowired
	private RemoteUserService userService;
    @Autowired
	private SmtOrganizeAccessService organizeAccessService;
	@Override
    protected OrganizeRelationRespDTO warp(SmtOrganizeRelation smtOrganizeRelation) throws IOException {
		OrganizeRelationRespDTO respDTO = BeanUtils.transform(OrganizeRelationRespDTO.class, smtOrganizeRelation);
		SmtPark park = parkService.getById(smtOrganizeRelation.getParkId());
		Result<UserInfo> result = userService.info(smtOrganizeRelation.getUserName(), SecurityConstants.FROM_IN);
		if(Objects.nonNull(result.getData())) {
			List<RoleDTO> roles = result.getData().getRoleList();
			List<String> strings = roles.stream().map(RoleDTO::getRoleName).collect(Collectors.toList());
			respDTO.setUserRole(strings.toString());
		}
		respDTO.setParkName(park.getParkName());
		respDTO.setSource(OrganizeSourceEnum.desc(smtOrganizeRelation.getSource()));
		respDTO.setCompType(smtOrganizeRelation.getCompType());
		respDTO.setPassword(null);
		respDTO.setDeviceAuthId(organizeAccessService.getDeviceAuthId(smtOrganizeRelation.getId()));
        return respDTO;
    }
}
