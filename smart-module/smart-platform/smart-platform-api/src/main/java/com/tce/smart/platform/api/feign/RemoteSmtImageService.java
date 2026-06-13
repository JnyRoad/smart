package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.SaveImageReqDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/***
 * description: 总部图片存取服务 <br>
 * date: 2019/12/12 17:40 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteSmtImageService {

	/**
	 * 根据图片编码获取Base64原图
	 *
	 * @param imageCode 图片编号
	 * @return 图片base64字符串
	 */
	@GetMapping("/image/get/base64/code/{imageCode}")
	Result<String> getImageBase64ByCode(@PathVariable("imageCode") String imageCode,
										@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 存储图片
	 * @param saveImageReqDto 图片信息
	 */
	@PostMapping("/image/save")
	Result<String> saveImage(@RequestBody SaveImageReqDto saveImageReqDto,
							 @RequestHeader(SecurityConstants.FROM) String from);
}
