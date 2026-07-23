package com.tce.smart.app.service.fore.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.tce.smart.algorithm.api.dto.req.CompareDTO;
import com.tce.smart.algorithm.api.dto.req.CompareImageDTO;
import com.tce.smart.algorithm.api.enums.AlgorithmTypeEnum;
import com.tce.smart.algorithm.api.enums.FaceTypeEnum;
import com.tce.smart.algorithm.api.feign.RemoteAlgorithmService;
import com.tce.smart.app.api.entity.AppUserDevice;
import com.tce.smart.app.service.AppSmsService;
import com.tce.smart.app.service.fore.DeviceManageService;
import com.tce.smart.app.service.fore.PasswordService;
import com.tce.smart.app.vo.fore.ChackFacePwdVo;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.EncDecryUtils;
import com.tce.smart.common.core.util.UUIDUtils;
import com.tce.smart.platform.api.dto.resp.InternalStaffPasswordRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffPhoneRespDTO;
import com.tce.smart.platform.api.feign.RemoteSmtImageService;
import com.tce.smart.platform.api.feign.RemoteStaffInternalService;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 密码服务实现类
 *
 * @author mckaywu
 * @date 2019-06-15 16:11:47
 */
@Service
@Slf4j
public class PasswordServiceImpl implements PasswordService {

	@Autowired
	private AppSmsService appSmsService;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	private RemoteStaffInternalService remoteStaffInternalService;

	@Autowired
	private RemoteAlgorithmService remoteAlgorithmService;

	@Autowired
	private RemoteSmtImageService remoteSmtImageService;

	@Autowired
	private DeviceManageService deviceManageService;

	@Value("${spring.face.forget-password}")
	private Double compareValue;

	@Value("${security.auth-code.encode-key:}")
	private String authCodeEncodeKey;

	@Override
	public String queryMobile(String badge) {
		return passwordPhone(badge).getMaskedPhone();
	}

	@Override
	public Boolean sendSmsCode(String badge) {
		// 手机号仅在服务端取得，客户端不得提交或获知完整值。
		appSmsService.sendSmsCode(passwordPhone(badge).getPhone());
		return Boolean.TRUE;
	}

	@Override
	public String verifySmsCode(String badge, String smsCode) {
		// 校验短信验证码
		appSmsService.verifySmsCode(passwordPhone(badge).getPhone(), smsCode);

		// 短信验证校验成功授权码
		String verifySuccessCode = saveRedisCode(badge);

		return URLEncoder.encode(EncDecryUtils.encryptByJasypt(verifySuccessCode, authCodeEncodeKey));
	}

	@Override
	public ChackFacePwdVo verifyFace(String facePhoto, String deviceNo) {
		if (StringUtil.isNullOrEmpty(facePhoto) || StringUtil.isNullOrEmpty(deviceNo)) {
			throw new TCEException("参数不全");
		}

		List<AppUserDevice> userDeviceList = deviceManageService.queryByDeviceNo(deviceNo);
		if (CollectionUtils.isEmpty(userDeviceList)) {
			throw new TCEException("获取设备信息异常");
		}
		//只取最近登录过的，优先已绑定的
		AppUserDevice appUserDevice = userDeviceList.get(0);

		Result<InternalStaffPasswordRespDTO> passwordStaffResponse;
		String badge;
		String staffFaceImgId;
		try {
			// 远程调用查询员工信息
			passwordStaffResponse = remoteStaffInternalService.getPasswordStaff(appUserDevice.getBadge(),
					SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
			if (!passwordStaffResponse.isSuccess() || Objects.isNull(passwordStaffResponse.getData())) {
				throw new TCEException("查询员工信息异常");
			}

			badge = passwordStaffResponse.getData().getBadge();
			staffFaceImgId = passwordStaffResponse.getData().getFacePicId();

			//下载员工人脸图片
			Result<String> getImageBase64Rs = remoteSmtImageService.getImageBase64ByCode(staffFaceImgId, SecurityConstants.FROM_IN);
			log.info("找回密码人脸图片读取完成 scene=forget-password success={}", getImageBase64Rs.isSuccess());
			if (!getImageBase64Rs.isSuccess() || StringUtil.isNullOrEmpty(getImageBase64Rs.getData())) {
				throw new TCEException("下载员工人脸图异常");
			}

			CompareDTO compareDTO = new CompareDTO();
			CompareImageDTO compareImageA = new CompareImageDTO();
			compareImageA.setImageBase64(facePhoto);
			compareImageA.setFaceType(FaceTypeEnum.LIVE.getType());
			CompareImageDTO compareImageB = new CompareImageDTO();
			compareImageB.setImageBase64(getImageBase64Rs.getData());
			compareImageB.setFaceType(FaceTypeEnum.LIVE.getType());
			compareDTO.setCompareImageA(compareImageA);
			compareDTO.setCompareImageB(compareImageB);

			// 人脸照片1:1对比
			//log.info("verifyFace 对比请求参数:[{}]",JSONUtil.toJsonStr(compareDTO));
			Result<com.tce.smart.algorithm.api.dto.resp.CompareDTO> result = remoteAlgorithmService.compare(UUIDUtils.create(),
					AlgorithmTypeEnum.COMPARE_FACEALL.getType(), compareDTO, SecurityConstants.FROM_IN);
			log.info("找回密码人脸比对完成 scene=forget-password success={}", result.isSuccess());

			if (result.isSuccess()) {
				//小于阀值则认为不是本人
				if (-1 == (new BigDecimal(String.valueOf(result.getData()))
						.compareTo(new BigDecimal(compareValue)))) {
					throw new TCEException("人脸不匹配,请重新拍照");
				}
			}
		} catch (TCEException tce) {
			throw tce;
		} catch (Exception e) {
			log.error("获取员工信息异常", e);
			throw new TCEException("获取员工信息异常");
		}
		String verifySuccessCode = saveRedisCode(badge);

		ChackFacePwdVo chackFacePwdVo = new ChackFacePwdVo();
		chackFacePwdVo.setUsername(badge);
		chackFacePwdVo.setPwdUpdateAuthCode(URLEncoder.encode(EncDecryUtils.encryptByJasypt(verifySuccessCode, authCodeEncodeKey)));
		return chackFacePwdVo;
	}

	/**
	 * 生成校验码，并存放redis
	 * @param badge 员工号
	 * @return 校验码
	 */
	private String saveRedisCode(String badge) {
		// 授权码
		String verifySuccessCode = RandomUtil.randomStringUpper(6);

		Map<String, Object> authCodeMap = new HashMap<>();
		authCodeMap.put(SecurityConstants.PWD_UPDATE_AUTHCODE_SUB_KEY, verifySuccessCode);
		String pwdUpdateAuthCodeKey = SecurityConstants.APP_PWD_UPDATE_AUTHCODE + badge;

		// 存放redis
		stringRedisTemplate.opsForValue().set(pwdUpdateAuthCodeKey, JSONUtil.toJsonStr(authCodeMap), 300,
				TimeUnit.SECONDS);// 5分钟分钟失效
		return verifySuccessCode;
	}

	/**
	 * 检查设备是否已绑定
	 *
	 * @param deviceNo 设备号
	 * @param badge    员工工号
	 * @return true-已绑定,false-未绑定
	 */
	private boolean checkDevcieBind(String deviceNo, String badge) {
		boolean isBind = false;
		List<AppUserDevice> bindDeviceList = deviceManageService.queryBindDevice(badge);
		if (CollectionUtils.isNotEmpty(bindDeviceList)) {
			for (AppUserDevice tempUserDevice : bindDeviceList) {
				if (deviceNo.equals(tempUserDevice.getDeviceNo())) {
					isBind = true;
					break;
				}
			}
		}

		return isBind;
	}

	private InternalStaffPhoneRespDTO passwordPhone(String badge) {
		Result<InternalStaffPhoneRespDTO> result = remoteStaffInternalService.getPasswordPhone(badge,
				SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if (!result.isSuccess() || Objects.isNull(result.getData()) || StringUtils.isBlank(result.getData().getPhone())) {
			throw new TCEException("获取员工信息异常");
		}
		return result.getData();
	}

}
