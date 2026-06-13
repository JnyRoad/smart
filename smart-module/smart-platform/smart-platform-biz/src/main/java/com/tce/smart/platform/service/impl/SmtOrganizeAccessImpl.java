package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.platform.core.entity.SmtOrganizeAccess;
import com.tce.smart.platform.core.mapper.SmtOrganizeAccessMapper;
import com.tce.smart.platform.service.SmtOrganizeAccessService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/2 17:00
 */
@Service
@AllArgsConstructor
@Slf4j
public class SmtOrganizeAccessImpl extends ServiceImpl<SmtOrganizeAccessMapper, SmtOrganizeAccess> implements SmtOrganizeAccessService {
	@Override
	public List<Integer> getDeviceAuthId(Long organizeId) {
		List<SmtOrganizeAccess> organizeAccessList = this.list(Wrappers.<SmtOrganizeAccess>lambdaQuery()
				.eq(SmtOrganizeAccess::getOrganizeId, organizeId));
		if(CollectionUtils.isNotEmpty(organizeAccessList)) {
			return organizeAccessList.stream().map(SmtOrganizeAccess::getDeviceAuthId).collect(Collectors.toList());
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public Boolean delByOrgId(Long organizeId) {
		return this.remove(Wrappers.<SmtOrganizeAccess>lambdaQuery()
				.eq(SmtOrganizeAccess::getOrganizeId, organizeId));
	}
}
