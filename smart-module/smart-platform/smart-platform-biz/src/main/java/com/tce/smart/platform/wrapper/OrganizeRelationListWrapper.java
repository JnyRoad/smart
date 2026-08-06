package com.tce.smart.platform.wrapper;

import cn.hutool.core.collection.CollUtil;
import com.tce.smart.admin.api.dto.InternalUserSummaryRespDTO;
import com.tce.smart.admin.api.feign.RemoteUserInternalService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.OrganizeRelationListRespDTO;
import com.tce.smart.platform.api.dto.resp.OrganizeRelationRespDTO;
import com.tce.smart.platform.core.entity.SmtOrganizeRelation;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.tool.enums.OrganizeSourceEnum;
import com.tce.smart.tool.enums.TempCompTypeEnum;
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
public class OrganizeRelationListWrapper extends BaseWrapper<SmtOrganizeRelation, OrganizeRelationListRespDTO> {
	@Autowired
	private SmtParkService parkService;
	@Autowired
	private RemoteUserInternalService userService;
    @Override
    protected OrganizeRelationListRespDTO warp(SmtOrganizeRelation smtOrganizeRelation) throws IOException {
		OrganizeRelationListRespDTO respDTO = BeanUtils.transform(OrganizeRelationListRespDTO.class, smtOrganizeRelation);
		SmtPark park = parkService.getById(smtOrganizeRelation.getParkId());
		Result<InternalUserSummaryRespDTO> result = userService.summary(smtOrganizeRelation.getUserName());
		if(Objects.nonNull(result.getData())) {
			List<String> strings = result.getData().getRoleNames();
			if(CollUtil.isNotEmpty(strings)) {
				respDTO.setUserRole(strings.get(0));
			}
		}
		respDTO.setParkName(park.getParkName());
		respDTO.setCompType(smtOrganizeRelation.getCompType());
		respDTO.setCompTypeDesc(TempCompTypeEnum.desc(smtOrganizeRelation.getCompType()));
        return respDTO;
    }
}
