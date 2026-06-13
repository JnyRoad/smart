package com.tce.smart.data.api.feign.ehrview;


import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYscompRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author WangJinbo123
 * @since 2019-05-03
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteOvwYscompService {

    /**
     * 根据compId获取公司信息
     * @param compId
     * @return
     */
    @GetMapping("/ys/comp/info")
    Result<OvwYscompRespDTO> getByCompId(@RequestParam("compId") String compId,
										 @RequestHeader(SecurityConstants.FROM) String from);
    /**
     * 获取所有公司信息
     * @return
     */
    @GetMapping("/ys/comp/list")
    Result<List<OvwYscompRespDTO>> getList(@RequestHeader(SecurityConstants.FROM) String from);
}
