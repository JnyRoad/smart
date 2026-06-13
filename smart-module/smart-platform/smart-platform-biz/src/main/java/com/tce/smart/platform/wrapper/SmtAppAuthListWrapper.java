package com.tce.smart.platform.wrapper;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.vo.msg.SmtAppAuthListVo;
import com.tce.smart.platform.core.entity.SmtAppAuth;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.tool.enums.AppAuthInitFlagnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

/**
 * App权限列表消息包装类
 *
 * @author mckaywu
 * @date 2019-06-13 11:52:31
 */
@Component
public class SmtAppAuthListWrapper extends BaseWrapper<SmtAppAuth, SmtAppAuthListVo> {

	@Autowired
	private SmtParkService parkService;

	@Override
	protected SmtAppAuthListVo warp(SmtAppAuth smtAppAuth) throws IOException {
		SmtAppAuthListVo smtAppAuthListVo = new SmtAppAuthListVo();
		smtAppAuthListVo.setId(smtAppAuth.getId());
		smtAppAuthListVo.setAuthName(smtAppAuth.getAuthName());

		// 是否固定，不能删除
		Boolean isFix = AppAuthInitFlagnum.INIT.getCode().intValue() == smtAppAuth.getInitFlag().intValue();
		smtAppAuthListVo.setIsFix(isFix);

		smtAppAuthListVo.setAuthDesc(smtAppAuth.getAuthDesc());
		smtAppAuthListVo.setCreateTime(smtAppAuth.getCreateTime());
		SmtPark smtPark = parkService.getById(smtAppAuth.getParkId());
		if(Objects.nonNull(smtPark)) {
			smtAppAuthListVo.setParkName(smtPark.getParkName());
		}
		return smtAppAuthListVo;
	}
}
