package com.tce.smart.platform.service.dormitoryconfig.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.platform.api.dto.req.dormitoryconfig.DormitoryPersonReqDTO;
import com.tce.smart.platform.core.entity.dormitoryconfig.SmtDormitoryPerson;
import com.tce.smart.platform.core.mapper.SmtDormitoryPersonMapper;
import com.tce.smart.platform.service.dormitoryconfig.SmtDormitoryPersonService;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.util.ToolUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author fushiping
 * @date 2021-09-14 20:14:59
 */
@Service
public class SmtDormitoryPersonServiceImpl extends ServiceImpl<SmtDormitoryPersonMapper, SmtDormitoryPerson> implements SmtDormitoryPersonService {

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean editPerson(List<DormitoryPersonReqDTO> personList, Long configId) {
		this.remove(Wrappers.<SmtDormitoryPerson>query().lambda().eq(SmtDormitoryPerson::getConfigId, configId));
		if(CollUtil.isEmpty(personList)) {
			return Boolean.FALSE;
		}
		for(DormitoryPersonReqDTO personReq : personList) {
			SmtDormitoryPerson smtDormitoryPerson = BeanUtils.transform(SmtDormitoryPerson.class, personReq);
			smtDormitoryPerson.setConfigId(configId);
			if(CollUtil.isNotEmpty(personReq.getDormitoryIds())) {
				smtDormitoryPerson.setDormitoryIds(StringUtils.join(personReq.getDormitoryIds(), SymbolConstants.COMMA));
			}
			this.save(smtDormitoryPerson);
		}
		return Boolean.TRUE;
	}

	@Override
	public List<SmtDormitoryPerson> getByConfigId(Long configId) {
		return this.list(Wrappers.<SmtDormitoryPerson>query().lambda().eq(SmtDormitoryPerson::getConfigId, configId));
	}

	@Override
	public List<Integer> getDormitoryId(String account, Integer parkId) {
		List<SmtDormitoryPerson> personList = this.list(Wrappers.<SmtDormitoryPerson>query().lambda()
				.eq(StrUtil.isNotBlank(account), SmtDormitoryPerson::getAccount, account)
				.eq(Objects.nonNull(parkId), SmtDormitoryPerson::getParkId, parkId));
		if(CollUtil.isEmpty(personList)) {
			return new ArrayList<>();
		}
		List<Integer> dormitoryId = new ArrayList<>();
		for(SmtDormitoryPerson person : personList) {
			if(StrUtil.isEmpty(person.getDormitoryIds())) {
				continue;
			}
			List<Integer> integers = ToolUtils.splitInt(person.getDormitoryIds());
			dormitoryId.addAll(integers);
		}
		return dormitoryId;
	}

	@Override
	public List<Integer> getParkId(String account) {
		List<SmtDormitoryPerson> personList = this.getByAccount(account);
		if(CollUtil.isEmpty(personList)) {
			return new ArrayList<>();
		}
		return personList.stream().map(SmtDormitoryPerson::getParkId).collect(Collectors.toList());
	}

	private List<SmtDormitoryPerson> getByAccount(String account) {
		return this.list(Wrappers.<SmtDormitoryPerson>query().lambda().eq(SmtDormitoryPerson::getAccount, account));
	}
}
