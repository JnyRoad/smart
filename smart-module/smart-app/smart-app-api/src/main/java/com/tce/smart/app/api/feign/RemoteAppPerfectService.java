package com.tce.smart.app.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.tce.smart.app.api.dto.AddIdCollectDto;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;

/**
 * App信息完善服务接口
 *
 * @author mckaywu
 * @date 2019-06-13 15:35:34
 */
@FeignClient(value = ServiceNameConstants.APP_SERVICE)
public interface RemoteAppPerfectService {

	/**
	 * 收集员工信息-人脸
	 *
	 * @param addIdCollectDto 人脸信息
	 * @return true-成功，fasle-失败
	 */
	@PostMapping("/perfect/collect/face")
	Result<Boolean> saveFaceCollect(@RequestBody AddIdCollectDto addIdCollectDto);

}
