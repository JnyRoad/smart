package com.tce.smart.platform.wrapper;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.vo.msg.SmtAppAuthDetailVo;
import com.tce.smart.platform.core.entity.SmtAppAuth;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * App权限列表消息包装类
 *
 * @author mckaywu
 * @date 2019-06-13 11:52:31
 */
@Component
public class SmtAppAuthDetailWrapper extends BaseWrapper<SmtAppAuth, SmtAppAuthDetailVo> {

	@Override
	protected SmtAppAuthDetailVo warp(SmtAppAuth smtAppAuth) throws IOException {
		SmtAppAuthDetailVo smtAppAuthDetailVo = new SmtAppAuthDetailVo();
		smtAppAuthDetailVo.setId(smtAppAuth.getId());
		smtAppAuthDetailVo.setAuthName(smtAppAuth.getAuthName());

		// 模块ID集合
		String[] moduleIdList = null;
		if (StringUtils.isNotBlank(smtAppAuth.getModuleId())) {
			moduleIdList = smtAppAuth.getModuleId().split(",");
		}
		smtAppAuthDetailVo.setModuleId(moduleIdList);

		// HR招聘权限集合
		String[] hrAuthIdList = null;
		if (StringUtils.isNotBlank(smtAppAuth.getHrAuthId())) {
			hrAuthIdList = smtAppAuth.getHrAuthId().split(",");
		}
		smtAppAuthDetailVo.setHrAuthId(hrAuthIdList);

		// 是否固定，不能删除
//		Boolean isFix = AppAuthInitFlagnum.INIT.getCode().intValue() == smtAppAuth.getInitFlag().intValue();
//		smtAppAuthDetailVo.setIsFix(smtAppAuthService.getInitFlag(smtAppAuth.getParkId()));
		smtAppAuthDetailVo.setInitFlag(smtAppAuth.getInitFlag());

		smtAppAuthDetailVo.setAuthDesc(smtAppAuth.getAuthDesc());
		smtAppAuthDetailVo.setCreateTime(smtAppAuth.getCreateTime());
		smtAppAuthDetailVo.setParkId(smtAppAuth.getParkId());

		// 职层集合
		String[] jcheIdList = null;
		if(StringUtils.isNotBlank(smtAppAuth.getJcheId())) {
			jcheIdList = smtAppAuth.getJcheId().split(",");
		}
		smtAppAuthDetailVo.setJcheId(jcheIdList);

		return smtAppAuthDetailVo;
	}
}
