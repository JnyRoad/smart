package com.tce.smart.platform.service.securityzone.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityAuthRelationReqDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthRelation;
import com.tce.smart.platform.core.mapper.SmtSecurityAuthRelationMapper;
import com.tce.smart.platform.service.securityzone.SmtSecurityAuthRelationService;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:12:53
 */
@Service
public class SmtSecurityAuthRelationServiceImpl extends ServiceImpl<SmtSecurityAuthRelationMapper, SmtSecurityAuthRelation> implements SmtSecurityAuthRelationService {

	@Override
	public Boolean editAuth(List<SmtSecurityAuthRelation> dto) {
		return this.saveBatch(dto);
	}

	@Override
	public List<SmtSecurityAuthRelation> getList(Long securityZoneId) {
		return this.list(Wrappers.<SmtSecurityAuthRelation>query().lambda()
				.eq(SmtSecurityAuthRelation::getSecurityId, securityZoneId));
	}

	@Override
	public List<SmtSecurityAuthRelation> getBatchList(List<Long> securityZoneId) {
		return this.list(Wrappers.<SmtSecurityAuthRelation>query().lambda()
				.in(SmtSecurityAuthRelation::getSecurityId, securityZoneId));
	}

	@Override
	public Boolean deleteAuth(Long securityZoneId) {
		return this.remove(Wrappers.<SmtSecurityAuthRelation>query().lambda()
				.eq(SmtSecurityAuthRelation::getSecurityId, securityZoneId));
	}

	@Override
	public Boolean batchDeleteAuth(List<Long> securityZoneIds) {
		return this.remove(Wrappers.<SmtSecurityAuthRelation>query().lambda()
				.in(SmtSecurityAuthRelation::getSecurityId, securityZoneIds));
	}
}
