package com.tce.smart.data.api.feign.ehrview;


import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsdepRespDTO;
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
public interface RemoteOvwYsdepService {
    /**
     * 根据compId获取部门信息
     * @param compId
     * @return
     */
    @GetMapping("/ys/dep/comp")
    Result<List<OvwYsdepRespDTO>> getByCompId(@RequestParam("compId") Integer compId,
											  @RequestHeader(SecurityConstants.FROM) String from);
    /**
     * 根据depId获取部门信息
     * @return
     */
    @GetMapping("/ys/dep/info")
    Result<OvwYsdepRespDTO> getByDepId(@RequestParam("depId") Integer depId, @RequestHeader(SecurityConstants.FROM) String from);

    @GetMapping("/ys/dep/parentDep")
    Result<List<OvwYsdepRespDTO>> getParentDep(@RequestParam("depId") Integer depId,
                                               @RequestHeader(SecurityConstants.FROM) String from);


}
