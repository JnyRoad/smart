package com.tce.smart.data.api.feign.temporary;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.temporary.req.EleaveJjitemReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.validation.Valid;
import java.util.List;

/**
 *
 * @date 2018/6/22
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteEleaveJjitemService {
	/**
	 * 保存工作交接项信息
	 * @param eleaveJjitemReqDTO
	 * @param from
	 * @return Result
	 */
    @PostMapping("/eleaveJjitem/internal/save")
	Result<Boolean> save(@Valid @RequestBody EleaveJjitemReqDTO eleaveJjitemReqDTO,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

    /**
     * 批量保存工作交接项信息
     * @param entityList
     * @param from
     * @return Result
     */
    @PostMapping("/eleaveJjitem/internal/save/batch")
	Result<Boolean> save(@RequestBody List<EleaveJjitemReqDTO> entityList,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

}
