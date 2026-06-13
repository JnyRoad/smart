package com.tce.smart.platform.wrapper;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.vo.msg.SmtAppHrAuthListVo;
import com.tce.smart.platform.core.entity.SmtAppHrAuth;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * App HR招聘数据权限列表消息包装类
 *
 * @author mckaywu
 * @date 2019-06-13 11:52:31
 */
@Component
public class SmtAppHrAuthListWrapper extends BaseWrapper<SmtAppHrAuth, SmtAppHrAuthListVo> {

	@Override
	protected SmtAppHrAuthListVo warp(SmtAppHrAuth smtAppHrAuth) throws IOException {
		SmtAppHrAuthListVo smtAppHrAuthListVo = new SmtAppHrAuthListVo();
		smtAppHrAuthListVo.setId(smtAppHrAuth.getId());
		smtAppHrAuthListVo.setAuthName(smtAppHrAuth.getAuthName());
		smtAppHrAuthListVo.setCreateTime(smtAppHrAuth.getCreateTime());

		return smtAppHrAuthListVo;
	}
}
