package com.tce.smart.app.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.api.dto.*;
import com.tce.smart.app.dto.WechatAccessTokenDto;
import com.tce.smart.app.entity.AppWechatBinding;
import com.tce.smart.app.mapper.AppWechatBindingMapper;
import com.tce.smart.app.service.AppCommService;
import com.tce.smart.app.service.AppSmsService;
import com.tce.smart.app.service.AppWechatBindingService;
import com.tce.smart.app.service.wechat.WechatAuthService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.SaveImageReqDto;
import com.tce.smart.platform.api.dto.req.WechatBandingReqDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffBindingRespDTO;
import com.tce.smart.platform.api.dto.resp.WechatBandingRespDTO;
import com.tce.smart.platform.api.feign.RemoteSmtImageService;
import com.tce.smart.platform.api.feign.RemoteStaffInternalService;
import com.tce.smart.platform.api.feign.RemoteWechatBandingService;
import com.tce.smart.tool.enums.SmtImageEnum;
import com.tce.smart.tool.enums.StaffStatusEnum;
import com.tce.smart.tool.exception.TCEException;
import com.tce.smart.tool.util.ToolUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @description: 微信绑定Service实现
 * @date: 2020-08-06 17:47
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@Service
@AllArgsConstructor
public class AppWechatBindingServiceImpl extends ServiceImpl<AppWechatBindingMapper, AppWechatBinding> implements AppWechatBindingService {

	private final WechatAuthService wechatAuthService;

	private final RemoteSmtImageService smtImageService;

	private final AppSmsService appSmsService;

	private final AppCommService appCommService;

	private final RemoteWechatBandingService remoteWechatBandingService;

	private final RemoteStaffInternalService remoteStaffInternalService;

	@Override
    public WechatBindingInfoDTO isWechatBinding(String code) {
		WechatAccessTokenDto accessTokenByCode = wechatAuthService.getAccessTokenByCode(code);
		AppWechatBinding appWechatBinding = this.baseMapper.selectOne(new LambdaQueryWrapper<AppWechatBinding>().eq(AppWechatBinding::getOpenId, accessTokenByCode.getOpenId()));
		WechatBindingInfoDTO wechatBindingInfoDTO = new WechatBindingInfoDTO();
		wechatBindingInfoDTO.setIsBinding(false);
		if(null != appWechatBinding){
			wechatBindingInfoDTO.setIsBinding(true);
			wechatBindingInfoDTO.setVisitPhone(appWechatBinding.getVisitPhone());
			//如果用户没有更换过图片 返回前端null 前端显示默认图片
			if(StringUtils.isNotEmpty(appWechatBinding.getImageCode())) {
				wechatBindingInfoDTO.setVisitImgUrl(appCommService.buildHqImageUrl(appWechatBinding.getImageCode()));
			}
		}
		return wechatBindingInfoDTO;
    }

	@Override
	public boolean saveWechatBinding(WechatBindingReqDTO wechatBindingReqDTO) {
		//获取openid
		WechatAccessTokenDto accessTokenByCode = wechatAuthService.getAccessTokenByCode(wechatBindingReqDTO.getCode());

		//判断验证码
		Boolean isSucc = appSmsService.verifySmsCode(wechatBindingReqDTO.getVisitPhone(), wechatBindingReqDTO.getVerifCode());
		if(!isSucc){
			log.error("绑定微信，验证码错误");
			return false;
		}

		//保存绑定信息
		return this.save(AppWechatBinding.builder()
				.openId(accessTokenByCode.getOpenId())
				.visitPhone(wechatBindingReqDTO.getVisitPhone())
				.createTime(new Date())
				.updateTime(new Date())
				.build());
	}

	@Override
	public boolean saveWechatOpenIdAndBadge(WechatOpenIdBindingReqDTO reqDTO) {
		String authInfo = wechatAuthService.getBadge(reqDTO.getCode());
		JSONObject authObj = JSONUtil.parseObj(authInfo);
		String openId = authObj.getStr("openId");
		if (openId == null) {
			throw new SmartException("您还未关注公众号,请先关注");
		}
		Result<InternalStaffBindingRespDTO> staffInfo = remoteStaffInternalService.getBindingStaff(reqDTO.getBadge(),
				SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if (!staffInfo.isSuccess() || staffInfo.getData() == null) {
			throw new SmartException("员工信息不存在");
		}
		Integer status = staffInfo.getData().getStatus();
		if (StaffStatusEnum.STAFF_STATUS_QUIT.getCode().equals(status)
				|| StaffStatusEnum.UNKNOWN.getCode().equals(status)) {
			throw new SmartException("员工状态异常");
		}
		if (StaffStatusEnum.STAFF_STATUS_IN.getCode().equals(status)
				|| StaffStatusEnum.STAFF_STATUS_TTRY.getCode().equals(status)
				|| StaffStatusEnum.STAFF_STATUS_PRACTICE.getCode().equals(status)) {
			throw new SmartException("请前往小程序绑定");
		}
		String certNoLast6 = staffInfo.getData().getCertNoLast6();
		if (StrUtil.isBlank(certNoLast6)) {
			throw new SmartException("员工身份证信息不存在");
		}
		if (!certNoLast6.equalsIgnoreCase(reqDTO.getLastCertNum())) {
			throw new SmartException("身份证信息不匹配");
		}
		Result<WechatBandingRespDTO> bandingInfo = remoteWechatBandingService.getByOpenId(reqDTO.getBadge(), SecurityConstants.FROM_IN);
		if (bandingInfo == null || !bandingInfo.isSuccess()) {
			throw new SmartException("查询绑定信息失败");
		}
		if (bandingInfo.getData() != null) {
			throw new SmartException("该工号已绑定其他微信账号");
		}
		WechatBandingReqDTO wechatBandingReqDTO = new WechatBandingReqDTO();
		wechatBandingReqDTO.setBadge(reqDTO.getBadge());
		wechatBandingReqDTO.setOpenId(openId);
		wechatBandingReqDTO.setParkId(reqDTO.getParkId());
		wechatBandingReqDTO.setUnionId(authObj.getStr("unionId"));
		Result<Boolean> saveRes = remoteWechatBandingService.save(wechatBandingReqDTO, SecurityConstants.FROM_IN);
		log.info("微信openId绑定完成");
		if (saveRes == null) {
			throw new SmartException("绑定失败");
		}
		return saveRes.getData();
	}

	@Override
	public boolean unbindWechatOpenIdAndBadge() {
		String username = SecurityUtils.getUser().getUsername();
		Result<WechatBandingRespDTO> bindInfo = remoteWechatBandingService.getByOpenId(username, SecurityConstants.FROM_IN);
		log.info("微信绑定查询结果：{}", bindInfo);
		if (bindInfo == null || !bindInfo.isSuccess() || bindInfo.getData() == null) {
			// 绑定信息不存在，可能是正式员工
			if (!ToolUtils.unbindWechatAndBadge(username)) {
				throw new SmartException("解绑失败");
			}
			return true;
		}
		Result<Boolean> unbindRes = remoteWechatBandingService.removeById(bindInfo.getData().getId());
		log.info("微信解绑结果：{}", unbindRes);
		if (unbindRes == null || !unbindRes.isSuccess()) {
			throw new SmartException("解绑失败");
		}
		return unbindRes.getData();
	}

	@Override
	public boolean updateBindingPhone(BindingPhoneReqDTO bindingPhoneReqDTO) {
		//判断验证码
		Boolean isSucc = appSmsService.verifySmsCode(bindingPhoneReqDTO.getNewPhone(), bindingPhoneReqDTO.getVerifCode());
		if(!isSucc){
			log.error("修改绑定手机号，验证码错误");
			return false;
		}
		//获取openid
		WechatAccessTokenDto accessTokenByCode = wechatAuthService.getAccessTokenByCode(bindingPhoneReqDTO.getCode());
		boolean res = this.update(AppWechatBinding.builder()
						.visitPhone(bindingPhoneReqDTO.getNewPhone()).build(),
				new LambdaUpdateWrapper<AppWechatBinding>().eq(AppWechatBinding::getOpenId,accessTokenByCode.getOpenId()));
		return res;
	}

	@Override
	public boolean updateImg(VisitorImgReqDTO visitorImgReqDTO) {
		//获取openid
		WechatAccessTokenDto accessTokenByCode = wechatAuthService.getAccessTokenByCode(visitorImgReqDTO.getCode());
		//保存图片
		Result<String> result = smtImageService.saveImage(SaveImageReqDto.builder()
				.base64String(visitorImgReqDTO.getImgs())
				.imageType(SmtImageEnum.TYPE_VISITOR_FACE.getCode())
				.build(),SecurityConstants.FROM_IN);
		if(!result.isSuccess()){
			log.error("远程调用图片存储异常:{}",result.getMessage());
			throw new TCEException("存储图片异常");
		}
		//修改
		boolean res = this.update(AppWechatBinding.builder()
						.imageCode(result.getData()).build(),
				new LambdaUpdateWrapper<AppWechatBinding>().eq(AppWechatBinding::getOpenId,accessTokenByCode.getOpenId()));
		return res;
	}
}
