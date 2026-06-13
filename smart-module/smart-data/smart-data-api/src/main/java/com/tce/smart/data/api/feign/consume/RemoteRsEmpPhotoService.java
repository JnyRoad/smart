package com.tce.smart.data.api.feign.consume;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.consume.req.UpdateHeadImageReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 短信服务
 *
 * @author mingkai.wu
 * @date 2019-05-15 18:54:18
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteRsEmpPhotoService {

	/**
	 * 更新员工头像
	 *
	 * @param headImageAo 头像信息
	 * @return ture-成功,false-失败
	 */
	@PostMapping("/rsempphoto/update/headimage")
	Result<Boolean> updateHeadImage(@RequestBody UpdateHeadImageReqDTO headImageAo,
									@RequestHeader(SecurityConstants.FROM) String from);
}
