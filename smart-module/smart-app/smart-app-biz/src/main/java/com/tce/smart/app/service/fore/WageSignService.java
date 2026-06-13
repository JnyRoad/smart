package com.tce.smart.app.service.fore;

import com.tce.smart.app.dto.fore.WageSignDto;
import com.tce.smart.common.core.model.Result;

/**
 * 工资签单接口
 * @author qipei
 *
 */
public interface WageSignService {

	/**
	 * 提交签名
	 * @param wageSignDto
	 * @return
	 */
	Result updateToSign(WageSignDto wageSignDto);
}
