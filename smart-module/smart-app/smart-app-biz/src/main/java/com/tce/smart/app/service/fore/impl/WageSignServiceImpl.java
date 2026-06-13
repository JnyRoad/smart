package com.tce.smart.app.service.fore.impl;

import cn.hutool.json.JSONUtil;
import com.tce.smart.app.dto.fore.WageSignDto;
import com.tce.smart.app.service.fore.WageSignService;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.SmtWageSignDTO;
import com.tce.smart.platform.api.feign.RemoteWageSignService;
import com.tce.smart.tool.exception.TCEException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 工资签单
 * @author qipei
 *
 */
@Service
@AllArgsConstructor
@Slf4j
public class WageSignServiceImpl  implements WageSignService{
	private final RemoteWageSignService remoteWageSignService;

	@Override
	public Result updateToSign(WageSignDto wageSignDto) {
		SmtWageSignDTO wageSign = new SmtWageSignDTO();
		wageSign.setBadge(wageSignDto.getBadge());
		wageSign.setWageDate(wageSignDto.getWageDate());
		wageSign.setSignImg(wageSignDto.getSignImg());
		Result<?> result = remoteWageSignService.updateToSign(wageSign, SecurityConstants.FROM_IN);
		if (CommonConstants.SUCCESS.equals(result.getCode()) ) {
			return result;
		}
		throw new TCEException(result.getCode(), result.getMsg());
	}
}
