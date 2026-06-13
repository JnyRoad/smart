package com.tce.smart.platform.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;

/**
 * 待审批
 * @author Lenovo
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteApproveListService {

    /**
     * 待审批记录
     * @param current 页号
     * @param size 返回数量
     * @param approveBadge 审批人员工号
     * @param approveType 审批类型
     * @return
     */
    @GetMapping("/approve/list/page")
    Result getProcessRecord(@RequestParam("current") Long current, @RequestParam("size") Long size, @RequestParam("approveBadge") String approveBadge, @RequestParam("approveType") Integer approveType, @RequestParam("approveState") Integer approveState, @RequestHeader(SecurityConstants.FROM) String from);

}
