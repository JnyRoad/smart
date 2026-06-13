package com.tce.smart.platform.service.securityzone.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityWhiteReqDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityWhite;
import com.tce.smart.platform.core.mapper.SmtSecurityWhiteMapper;
import com.tce.smart.platform.service.securityzone.SmtSecurityWhiteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Wrapper;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:07
 */
@Service
public class SmtSecurityWhiteServiceImpl extends ServiceImpl<SmtSecurityWhiteMapper, SmtSecurityWhite> implements SmtSecurityWhiteService {

	@Override
	public List<SmtSecurityWhite> getList(Long securityId) {
		return this.list(Wrappers.<SmtSecurityWhite>query().lambda().eq(SmtSecurityWhite::getDeleteConfigId, securityId));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean editList(List<SecurityWhiteReqDTO> req, Long deleteConfigId) {
		this.remove(Wrappers.<SmtSecurityWhite>query().lambda().eq(SmtSecurityWhite::getDeleteConfigId, deleteConfigId));
		if(CollUtil.isEmpty(req)) {
			return Boolean.TRUE;
		}
		List<SmtSecurityWhite> smtSecurityWhites = req.stream().map(whites -> {
			SmtSecurityWhite smtSecurityWhite = BeanUtils.transform(SmtSecurityWhite.class, whites);
			smtSecurityWhite.setDeleteConfigId(deleteConfigId);
			return smtSecurityWhite;
		}).collect(Collectors.toList());
		return this.saveBatch(smtSecurityWhites);
	}

	@Override
	public Boolean isExist(Long configId, Long staffId) {
		Integer count =  this.count(Wrappers.<SmtSecurityWhite>query().lambda()
				.eq(SmtSecurityWhite::getDeleteConfigId, configId)
				.eq(SmtSecurityWhite::getStaffId, staffId));
		return count > 0 ? Boolean.TRUE : Boolean.FALSE;
	}
}
