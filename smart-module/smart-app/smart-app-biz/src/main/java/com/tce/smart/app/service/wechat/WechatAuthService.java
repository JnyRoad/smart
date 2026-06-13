package com.tce.smart.app.service.wechat;

import com.tce.smart.app.api.dto.WxSignDTO;
import com.tce.smart.app.dto.WechatAccessTokenDto;
import com.tce.smart.app.dto.WechatUserInfoDto;

/**
 * 微信公众号授权服务接口
 *
 * @author mckaywu
 * @date 2019-05-22 10:57:44
 */
public interface WechatAuthService {

	/**
	 * 通过code换取网页授权access_token
	 *
	 * @param code 获取授权临时凭证
	 * @return WechatAccessTokenDto token信息
	 */
	WechatAccessTokenDto getAccessTokenByCode(String code);

	/**
	 * 通过code换取网页授权access_token-许昌园区
	 *
	 * @param code 获取授权临时凭证
	 * @return WechatAccessTokenDto token信息
	 */
	WechatAccessTokenDto getAccessTokenForXc(String code);

	/**
	 * 根据code获取工号
	 * @param code
	 * @return
	 */
	String getBadgeByCode(String code);

	/**
	 * 通过code获取工号
	 * @param code
	 * @return
	 */
	String getBadge(String code);

	/**
	 * 获取用户登录信息
	 * @param token
	 * @param openId
	 * @return
	 */
	WechatUserInfoDto getUserInfoByOpenId(String token, String openId);

	/**
	 * 根据code获得unionid
	 * @param code
	 * @return
	 */
	WechatUserInfoDto getUnionId(String code);

	/**
	 * 许昌园区绑定微信信息
	 * @param code
	 * @return
	 */
	Boolean bangingForXc(String code);

	/**
	 * 获取微信签名数据
	 * @param url
	 * @return
	 */
	WxSignDTO getWxSignByUrl(String url);
}
