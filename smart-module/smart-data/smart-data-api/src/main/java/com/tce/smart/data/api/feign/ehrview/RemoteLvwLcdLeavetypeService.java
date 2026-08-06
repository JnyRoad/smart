package com.tce.smart.data.api.feign.ehrview;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.LvwLcdLeavetypeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @date 2018/6/22
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteLvwLcdLeavetypeService {

	/**
	 * 根据请假的id获取数据时长单位
	 * @param id
	 * @param from
	 * @return
	 */
    @GetMapping("/lvw/lvwLcdLeavetype/info")
    Result<LvwLcdLeavetypeDTO> info(@RequestParam("id") Integer id,
                                    @RequestHeader(SecurityConstants.FROM) String from,
                                    @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

}
