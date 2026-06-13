package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtJcheAuth;
import com.tce.smart.platform.core.mapper.SmtJcheAuthMapper;
import com.tce.smart.platform.service.SmtJcheAuthService;
import com.tce.smart.tool.enums.BusinessAuthorityEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author fushiping
 * @date 2020/9/3 0003 11:13
 **/
@Slf4j
@Service
public class SmtJcheAuthServiceImpl extends ServiceImpl<SmtJcheAuthMapper, SmtJcheAuth> implements SmtJcheAuthService {

	@Override
	public Integer getJchebusinessCode(Integer jcheId, Integer parkId) {
		SmtJcheAuth auth = this.getOne(Wrappers.<SmtJcheAuth>query().lambda().eq(SmtJcheAuth::getJcheId, jcheId).eq(SmtJcheAuth::getParkId, parkId));
		if(Objects.nonNull(auth)) {
			return auth.getBusinessCode();
		}
		return BusinessAuthorityEnum.IN_OUT_STAFF_VEHICLE.getCode();
	}
}
