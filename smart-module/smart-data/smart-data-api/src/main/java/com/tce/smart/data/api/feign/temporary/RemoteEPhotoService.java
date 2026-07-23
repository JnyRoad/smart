package com.tce.smart.data.api.feign.temporary;

import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.temporary.req.SaveEPhotoReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * EHR员工照片远程服务接口
 *
 * @author mkwu
 * @date 2019-08-01
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteEPhotoService {

	/**
	 * 保存人事员工人脸图片信息
	 *
	 * @param saveEPhotoDto 保存EHR员工图片
	 * @return true-成功,false-失败
	 */
	@PostMapping("/ephoto/internal/save")
	Result<Boolean> saveOrUpdatePhoto(@RequestBody SaveEPhotoReqDTO saveEPhotoDto,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

}
