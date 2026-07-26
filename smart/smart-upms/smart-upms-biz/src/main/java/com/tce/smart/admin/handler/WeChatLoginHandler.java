package com.tce.smart.admin.handler;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.admin.api.dto.UserInfo;
import com.tce.smart.admin.api.entity.SysSocialDetails;
import com.tce.smart.admin.api.entity.SysUser;
import com.tce.smart.admin.mapper.SysSocialDetailsMapper;
import com.tce.smart.admin.service.SysUserService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.enums.LoginTypeEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component("WX")
@AllArgsConstructor
public class WeChatLoginHandler extends AbstractLoginHandler {
	private final RestTemplate restTemplate;
	private final SysUserService sysUserService;
	private final SysSocialDetailsMapper sysSocialDetailsMapper;

	/**
	 * 微信登录传入code
	 * <p>
	 * 通过code 调用qq 获取唯一标识
	 *
	 * @param code
	 * @return
	 */
	@Override
	public String identify(String code) {
		SysSocialDetails condition = new SysSocialDetails();
		condition.setType(LoginTypeEnum.WECHAT.getType());
		SysSocialDetails socialDetails = sysSocialDetailsMapper.selectOne(new QueryWrapper<>(condition));

		String url = String.format(SecurityConstants.WX_AUTHORIZATION_CODE_URL
			, socialDetails.getAppId(), socialDetails.getAppSecret(), code);
		String result = HttpUtil.get(url);
		// OAuth 响应可能包含 access token，只记录固定事件，禁止写入原始响应。
		log.debug("微信授权码交换响应已接收");

		Object obj = JSONUtil.parseObj(result).get("openid");
		return obj.toString();
	}

	/**
	 * openId 获取用户信息
	 *
	 * @param openId
	 * @return
	 */
	@Override
	public UserInfo info(String openId) {
		SysUser user = sysUserService
			.getOne(Wrappers.<SysUser>query()
				.lambda().eq(SysUser::getWxOpenid, openId));

		if (user == null) {
			log.info("微信未绑定");
			return null;
		}
		return sysUserService.findUserInfo(user);
	}
}
