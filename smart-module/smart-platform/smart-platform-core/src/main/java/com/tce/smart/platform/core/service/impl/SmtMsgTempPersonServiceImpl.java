package com.tce.smart.platform.core.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.dto.MsgPersonDTO;
import com.tce.smart.platform.core.entity.SmtMsgTempPerson;
import com.tce.smart.platform.core.mapper.SmtMsgTempPersonMapper;
import com.tce.smart.platform.core.service.SmtMsgTempPersonService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Li.JiaJun
 * @since 2022/9/26 14:31
 */
@Service
public class SmtMsgTempPersonServiceImpl extends ServiceImpl<SmtMsgTempPersonMapper, SmtMsgTempPerson> implements SmtMsgTempPersonService {

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean save(Integer tempId, List<MsgPersonDTO> personList) {
		this.remove(Wrappers.<SmtMsgTempPerson>lambdaQuery()
				.eq(SmtMsgTempPerson::getTempId, tempId));
		List<SmtMsgTempPerson> msgTempPeople = new ArrayList<>();
		for (MsgPersonDTO personDTO : personList) {
			SmtMsgTempPerson person = new SmtMsgTempPerson();
			person.setTempId(tempId);
			person.setBadge(personDTO.getBadge());
			person.setName(personDTO.getName());
			msgTempPeople.add(person);
		}
		return this.saveBatch(msgTempPeople);
	}

	@Override
	public List<MsgPersonDTO> getList(Integer tempId) {
		List<SmtMsgTempPerson> personList = this.list(Wrappers.<SmtMsgTempPerson>lambdaQuery()
				.eq(SmtMsgTempPerson::getTempId, tempId));
		if (CollUtil.isNotEmpty(personList)) {
			return personList.stream().map(e -> {
				MsgPersonDTO dto = new MsgPersonDTO();
				dto.setBadge(e.getBadge());
				dto.setName(e.getName());
				return dto;
			}).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	@Override
	public List<String> getByTempId(Integer tempId) {
		List<SmtMsgTempPerson> personList = this.list(Wrappers.<SmtMsgTempPerson>lambdaQuery()
				.eq(SmtMsgTempPerson::getTempId, tempId));
		if (CollUtil.isNotEmpty(personList)) {
			return personList.stream().map(SmtMsgTempPerson::getBadge).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
}
