package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.securityarea.SecurityAreaNotifyConfigDTO;
import com.tce.smart.platform.api.dto.req.securityarea.SecurityAreaNotifyListDTO;
import com.tce.smart.platform.api.dto.req.securityarea.SupplierNotifyStatusReqDTO;
import com.tce.smart.platform.api.dto.resp.SmtSecurityAreaSupplierRespDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 保密区供应商
 * @author wuling
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteSecurityAreaSupplierService {

	/**
	 * 查询需要通知的供应商列表
	 * @param notifyListDTO
	 * @return Result
	 */
	@PostMapping("/inner/notify/list")
	Result<List<SmtSecurityAreaSupplierRespDTO>> notifyList(@RequestBody SecurityAreaNotifyListDTO notifyListDTO);

	/**
	 * 查询所有保密区通知配置
	 * @return Result
	 */
	@GetMapping("/inner/notify/all/config")
	Result<List<SecurityAreaNotifyConfigDTO>> getNotifyAllConfig();

	/**
	 * 更新通知状态
	 * @param statusReqDTO
	 * @return Result
	 */
	@PostMapping("/inner/update/notify")
	Result<Boolean> updateNotifyStatus(@RequestBody SupplierNotifyStatusReqDTO statusReqDTO);
}
