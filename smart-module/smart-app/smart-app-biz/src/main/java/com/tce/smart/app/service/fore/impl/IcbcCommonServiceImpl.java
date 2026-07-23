package com.tce.smart.app.service.fore.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.icbc.api.IcbcApiException;
import com.icbc.api.IcbcConstants;
import com.icbc.api.UiIcbcClient;
import com.icbc.api.request.EaccountManageRequestV1;
import com.icbc.api.request.EaccountManageRequestV1.EaccountManageRequestBizV1;
import com.tce.smart.app.entity.AppIcbcConfig;
import com.tce.smart.app.service.AppIcbcConfigService;
import com.tce.smart.app.service.fore.IcbcCommonService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.resp.InternalStaffIdentityRespDTO;
import com.tce.smart.platform.api.feign.RemoteStaffInternalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 工商银行服务实现类
 *
 * @author mkwu
 * @date 2019-08-23
 */
@Service
@Slf4j
public class IcbcCommonServiceImpl implements IcbcCommonService {

	@Autowired
	private AppIcbcConfigService appIcbcConfigService;

	@Autowired
	private RemoteStaffInternalService remoteStaffInternalService;

	@Value("${spring.icbc.api.h5-eaccount}")
	private String eAccountUrl;

	@Override
	public String buildFormHtml() {
		String respContent = null;
		try {
			List<AppIcbcConfig> appIcbcConfigList = appIcbcConfigService.getCurrentConfig();
			if (CollectionUtil.isEmpty(appIcbcConfigList)) {
				return null;
			}
			AppIcbcConfig appIcbcConfig = appIcbcConfigList.get(0);

			String appId = appIcbcConfig.getAppId();
			String myPrivateKey = appIcbcConfig.getRsaPrivKey();
			String aesKey = appIcbcConfig.getAesKey();

			String badge = SecurityUtils.getUser().getUsername();
			Result<InternalStaffIdentityRespDTO> identityStaffResponse = remoteStaffInternalService.getIdentityStaff(badge,
					SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
			if (!identityStaffResponse.isSuccess() || Objects.isNull(identityStaffResponse.getData())) {
				return null;
			}

			String idCardNo = identityStaffResponse.getData().getCertno();// 身份证号

			UiIcbcClient client = new UiIcbcClient(appId, IcbcConstants.SIGN_TYPE_RSA2, myPrivateKey,
					IcbcConstants.CHARSET_UTF8, IcbcConstants.ENCRYPT_TYPE_AES, aesKey);
			EaccountManageRequestV1 request = new EaccountManageRequestV1();
			request.setServiceUrl(eAccountUrl);// 调整地址
			EaccountManageRequestBizV1 bizContent = new EaccountManageRequestBizV1();
			bizContent.setUserId(idCardNo);// 必输，需控制不能为空，用户唯一标识,送身份证号

			bizContent.setCorpAppid(appId);// 外公司合作方送APP_ID的值，行内应用如e支付等送外公司合作方的APPID

			String orderTimeStamp = buildReqTimeStamp(appIcbcConfig.getTestReqTime());
			bizContent.setOrderTimeStamp(orderTimeStamp);// 生成请求的时间,必输
			request.setBizContent(bizContent);
			respContent = client.buildPostForm(request);
			// 银行表单可能包含证件号，日志只记录生成结果，不能输出完整报文。
			log.info("工商银行实名表单生成完成 success={}", respContent != null);
		} catch (IcbcApiException e) {
			log.error("组织报文失败");
		}

		return respContent;
	}

	/**
	 * 构建请求时间参数
	 *
	 * @param tesDate 测试 时间
	 * @return 时间毫秒数
	 */
	@SuppressWarnings("deprecation")
	private String buildReqTimeStamp(Date tesDate) {
		if (Objects.isNull(tesDate)) {
			return String.valueOf(System.currentTimeMillis());
		} else {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.MONTH, tesDate.getMonth());
			calendar.set(Calendar.DAY_OF_MONTH, tesDate.getDate());
			return String.valueOf(calendar.getTimeInMillis());// 现时间戳
		}
	}
}
