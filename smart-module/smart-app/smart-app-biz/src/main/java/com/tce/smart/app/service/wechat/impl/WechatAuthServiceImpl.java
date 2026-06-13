package com.tce.smart.app.service.wechat.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.app.api.dto.WxSignDTO;
import com.tce.smart.app.dto.WechatAccessTokenDto;
import com.tce.smart.app.dto.WechatUserInfoDto;
import com.tce.smart.app.service.wechat.WechatAuthService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.platform.api.dto.req.WechatBandingReqDTO;
import com.tce.smart.platform.api.dto.resp.WechatBandingRespDTO;
import com.tce.smart.platform.api.feign.RemoteWechatBandingService;
import com.tce.smart.tool.util.ToolUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * 微信公众号授权服务实现类
 *
 * @author mckaywu
 * @date 2019-05-22 10:57:33
 */
@Service
@Slf4j
public class WechatAuthServiceImpl implements WechatAuthService {

	@Autowired
	private RemoteWechatBandingService remoteWechatBandingService;

	@Value("${spring.wechat.appId}")
	private String appId;

	@Value("${spring.wechat.appSecret}")
	private String appSecret;

	@Value("${spring.wechat.oauth2}")
	private String wechatOauth2;

	@Value("${spring.wechat.xc.userInfo}")
	private String userInfo;

	@Value("${spring.wechat.xc.appId}")
	private String xcAppId;

	@Value("${spring.wechat.xc.appSecret}")
	private String xcAppSecret;

	@Value("${spring.wechat.xc.baseUrl}")
	private String xcBaseUrl;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private CacheManager cacheManager;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean bangingForXc(String code) {
		String staffInfo = this.getBadge(code);
		JSONObject res = JSONUtil.parseObj(staffInfo);
		String openId = res.getStr("openId");
		if (openId == null) {
			throw new SmartException("您还未关注公众号,请先关注");
		}
		WechatBandingReqDTO reqDTO = new WechatBandingReqDTO();
		reqDTO.setBadge(res.getStr("no"));
		reqDTO.setOpenId(openId);
		try {
			remoteWechatBandingService.save(reqDTO, SecurityConstants.FROM_IN);
		}catch (Exception e) {
			throw new SmartException("用户信息绑定失败");
		}
		return Boolean.TRUE;
	}

	@Override
	public WechatAccessTokenDto getAccessTokenByCode(String code) {
		String requestUrl = wechatOauth2;
		requestUrl = requestUrl.replace("APPID", appId);
		requestUrl = requestUrl.replace("SECRET", appSecret);
		requestUrl = requestUrl.replace("CODE", code);
		return this.sendAccessTokenByCode(requestUrl);
	}

	/**
	 * 获取网页授权access_token，无次数限制，无需缓存
	 * @param code 获取授权临时凭证
	 * @return
	 */
	@Override
	public WechatAccessTokenDto getAccessTokenForXc(String code) {
		String requestUrl = wechatOauth2;
		requestUrl = requestUrl.replace("APPID", xcAppId);
		requestUrl = requestUrl.replace("SECRET", xcAppSecret);
		requestUrl = requestUrl.replace("CODE", code);
		return this.sendAccessTokenByCode(requestUrl);
	}

	@Override
	public WechatUserInfoDto getUnionId(String code) {
		WechatAccessTokenDto tokenDto = this.getAccessTokenForXc(code);
		return this.getUserInfoByOpenId(tokenDto.getAccessToken(), tokenDto.getOpenId());
	}

	@Override
	public WechatUserInfoDto getUserInfoByOpenId(String token, String openId) {
		WechatUserInfoDto userInfoDto = new WechatUserInfoDto();
		String requestUrl = userInfo.replace("ACCESS_TOKEN", token).replace("OPENID", openId);
		log.info("请求微信公众号userInfo");
		String response = restTemplate.getForObject(requestUrl, String.class);
		// 获取网页授权凭证
		if (StringUtils.isNotBlank(response)) {
			JSONObject jsonObject = JSONUtil.parseObj(response);
			try {
				userInfoDto = JSONUtil.toBean(jsonObject, WechatUserInfoDto.class);
			} catch (Exception e) {
				int errorCode = jsonObject.getInt("errcode");
				String errorMsg = jsonObject.getStr("errmsg");
				String errorInfo = "获取用户信息失败 errcode:{" + errorCode + "} errmsg:{" + errorMsg + "}";
				log.error("获取用户信息失败:({})", errorInfo);
				throw new TCEException(errorInfo);
			}
		}
		return userInfoDto;
	}


	/**
	 * 发送获取token请求
	 * @param requestUrl
	 * @return
	 */
	private WechatAccessTokenDto sendAccessTokenByCode(String requestUrl) {
		WechatAccessTokenDto wat = null;
		log.info("请求微信公众号accessToken");
//		String response = restTemplate.getForObject(requestUrl, String.class);
		String response = HttpUtil.get(requestUrl);
		// 获取网页授权凭证
		if (StringUtils.isNotBlank(response)) {
			JSONObject jsonObject = JSONUtil.parseObj(response);
			try {
				wat = new WechatAccessTokenDto();
				wat.setAccessToken(jsonObject.getStr("access_token"));
				wat.setExpiresIn(jsonObject.getLong("expires_in"));
				wat.setRefreshToken(jsonObject.getStr("refresh_token"));
				wat.setOpenId(jsonObject.getStr("openid"));
				wat.setScope(jsonObject.getStr("scope"));
			} catch (Exception e) {
				int errorCode = jsonObject.getInt("errcode");
				String errorMsg = jsonObject.getStr("errmsg");
				String errorInfo = "获取网页授权凭证失败 errcode:{" + errorCode + "} errmsg:{" + errorMsg + "}";
				log.error("请求微信公众号accessToken失败:({})", errorInfo);
				throw new TCEException(errorInfo);
			}
		}
		return wat;
	}

	@Override
	public String getBadgeByCode(String code) {
		String tokenInfo = getBadge(code);
		JSONObject tokenObj = JSONUtil.parseObj(tokenInfo);
		String badge = tokenObj.getStr("no");
		if (StrUtil.isNotBlank(badge)) {
			return badge;
		}
		String openId = tokenObj.getStr("openId");
		Result<WechatBandingRespDTO> badgeInfo = remoteWechatBandingService.getByOpenId(openId, SecurityConstants.FROM_IN);
		log.info("查询员工信息：{}", badgeInfo);
		if (!badgeInfo.isSuccess()) {
			throw new SmartException("查询员工信息失败");
		}
		return badgeInfo.getData() != null ? badgeInfo.getData().getBadge() : null;
	}

	@Override
	public String getBadge(String code) {
		return ToolUtils.getBadge(code);
	}

	@Override
	public WxSignDTO getWxSignByUrl(String url) {
		String ticket = getJsTicketForXC();
		return getWxSign(url, ticket, xcAppId);
	}

	/**
	 * 获取基础access_token，有次数限制（每天2000次），需要缓存
	 * 详情见：https://developers.weixin.qq.com/doc/offiaccount/Message_Management/API_Call_Limits.html
	 * @param appId
	 * @param appSecret
	 * @return
	 */
	private String getAccessToken(String appId, String appSecret) {
		Cache wxDetails = cacheManager.getCache("wx_details");
		Long tokenExpire = Objects.requireNonNull(wxDetails).get("token_expire_time", Long.class);
		long afterOneMinute = LocalDateTime.now().plusMinutes(1L).toEpochSecond(ZoneOffset.of("+8"));
		if (tokenExpire != null && afterOneMinute < tokenExpire) {
			return wxDetails.get("access_token", String.class);
		}
		String tokenUrl = SecurityConstants.WX_ACCESS_TOKEN_URL
				.replaceAll("APPID", appId)
				.replaceAll("APPSECRET", appSecret);
		String result = HttpUtil.get(tokenUrl);
		log.info("微信access_token请求完成");

		JSONObject resultObj = JSONUtil.parseObj(result);
		Integer errCode = resultObj.getInt("errcode");
		if (errCode != 0) {
			log.info("获取微信access_token失败,code:{}, msg:{}", errCode, resultObj.getStr("errmsg"));
			throw new SmartException("获取微信access_token失败");
		}
		// 获取到的凭证
		String accessToken = resultObj.getStr("access_token");
		// 凭证有效时间，单位：秒
		Integer expiresIn = resultObj.getInt("expires_in");
		wxDetails.put("access_token", accessToken);
		wxDetails.put("token_expire_time", LocalDateTime.now().plusSeconds(expiresIn).toEpochSecond(ZoneOffset.of("+8")));
		return accessToken;
	}

	private String getJsTicketForXC() {
		Cache wxDetails = cacheManager.getCache("wx_details");
		Long ticketExpire = Objects.requireNonNull(wxDetails).get("ticket_expire_time", Long.class);
		long afterOneMinute = LocalDateTime.now().plusMinutes(1L).toEpochSecond(ZoneOffset.of("+8"));
		if (ticketExpire != null && afterOneMinute < ticketExpire) {
			return wxDetails.get("js_ticket", String.class);
		}
		String ticketUrl = xcBaseUrl + "?key=getJsapiTicket";
		String ticketRes = HttpRequest.post(ticketUrl).execute().body();
		log.info("微信获取jsapi_ticket响应报文:{}", ticketRes);

		JSONObject res = JSONUtil.parseObj(ticketRes);
		String one = "1";
		String outCode = res.getStr("code");
		if(!one.equals(outCode)) {
			throw new SmartException("微信获取jsapi_ticket失败");
		}
		JSONObject data = res.getJSONObject("data");
		String inCode = data.getStr("code");
		if(!one.equals(inCode)) {
			throw new SmartException("微信获取jsapi_ticket失败");
		}
		String ticket = data.getStr("data");
		// 凭证有效时间，7200秒
		wxDetails.put("js_ticket", ticket);
		wxDetails.put("ticket_expire_time", LocalDateTime.now().plusSeconds(7200).toEpochSecond(ZoneOffset.of("+8")));
		return ticket;
	}

	private WxSignDTO getWxSign(String url, String ticket, String appId) {
		if (url != null && url.contains("#")) {
			url = url.substring(0, url.indexOf("#"));
		}
		String nonceStr = RandomUtil.randomString(16);
		Long timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
		String signSrc = "jsapi_ticket=" + ticket +
				"&noncestr=" + nonceStr +
				"&timestamp=" + timestamp +
				"&url=" + url;
		log.info("签名原数据：{}", signSrc);
		String signature = SecureUtil.sha1(signSrc);

		WxSignDTO signDTO = new WxSignDTO();
		signDTO.setAppId(appId);
		signDTO.setNonceStr(nonceStr);
		signDTO.setTimestamp(timestamp);
		signDTO.setSignature(signature);
		return signDTO;
	}
}
