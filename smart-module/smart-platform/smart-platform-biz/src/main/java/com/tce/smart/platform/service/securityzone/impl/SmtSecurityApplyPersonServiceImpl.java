package com.tce.smart.platform.service.securityzone.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityApplyPersonReqDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityApplyPerson;
import com.tce.smart.platform.core.mapper.SmtSecurityApplyPersonMapper;
import com.tce.smart.platform.service.securityzone.SmtSecurityApplyPersonService;
import com.tce.smart.tool.constant.SymbolConstants;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 *
 * @author fushiping
 * @date 2021-07-29 11:13:37
 */
@Service
public class SmtSecurityApplyPersonServiceImpl extends ServiceImpl<SmtSecurityApplyPersonMapper, SmtSecurityApplyPerson> implements SmtSecurityApplyPersonService {


	@Override
	public Boolean savePerson(List<SecurityApplyPersonReqDTO> personReq, Long applyId) {
		List<SmtSecurityApplyPerson> personList = personReq.stream().map(person -> {
			SmtSecurityApplyPerson applyPerson = BeanUtils.transform(SmtSecurityApplyPerson.class, person);
			applyPerson.setId(null);
			applyPerson.setApplyId(applyId);
			applyPerson.setCreateTime(LocalDateTime.now());
			List<Integer> authId = person.getApplyAuths().stream().map(SecurityApplyPersonReqDTO.ApplyAuth::getAuthId).collect(Collectors.toList());
			applyPerson.setAuthDetails(StringUtils.join(SymbolConstants.COMMA, authId));
			return applyPerson;
		}).collect(Collectors.toList());
		return this.saveBatch(personList);
	}
}
