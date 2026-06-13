package com.tce.smart.platform.wrapper.securityzone;


import cn.hutool.core.collection.CollUtil;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityZoneRespDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthRelation;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityZone;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.securityzone.SmtSecurityAuthRelationService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryCountByFloorWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
@AllArgsConstructor
public class SecurityZoneWrapper extends BaseWrapper<SmtSecurityZone, SecurityZoneRespDTO> {

	@Autowired
	private SmtSecurityAuthRelationService smtSecurityAuthRelationService;
	@Autowired
	private SmtParkService smtParkService;

    @Override
    protected SecurityZoneRespDTO warp(SmtSecurityZone bean) throws IOException {
		SecurityZoneRespDTO resp = BeanUtils.transform(SecurityZoneRespDTO.class, bean);
		List<SmtSecurityAuthRelation> relations = smtSecurityAuthRelationService.getList(bean.getId());
		SmtPark park = smtParkService.getById(bean.getParkId());
		resp.setParkName(park.getParkName());
		if(CollUtil.isNotEmpty(relations)) {
			List<String> authList = relations.stream().map(SmtSecurityAuthRelation::getAuthName).collect(Collectors.toList());
			resp.setAuthNameList(authList);
			List<SecurityZoneRespDTO.AuthList> authLists = new ArrayList<>();
			relations.forEach(relation ->{
				SecurityZoneRespDTO.AuthList auth = new SecurityZoneRespDTO.AuthList();
				auth.setAuthId(relation.getAuthId());
				auth.setAuthName(relation.getAuthName());
				authLists.add(auth);
			});
			resp.setAuthLists(authLists);
		}
        return resp;
    }
}
