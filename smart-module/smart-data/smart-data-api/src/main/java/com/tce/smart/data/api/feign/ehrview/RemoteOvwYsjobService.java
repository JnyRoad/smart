package com.tce.smart.data.api.feign.ehrview;


import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsjobRespDTO;
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
public interface RemoteOvwYsjobService {
    /**
     * 根据部门ID获取岗位信息
     * @param deptId
     * @return
     */
    @GetMapping("/ys/job/dept")
    Result<List<OvwYsjobRespDTO>> getByDeptId(@RequestParam("deptId") Integer deptId,
											  @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 根据岗位ID获取岗位信息
     * @param jobId
     * @return
     */
    @GetMapping("/ys/job/id")
    Result<OvwYsjobRespDTO> getByDeptName(@RequestParam("jobId") Integer jobId, @RequestHeader(SecurityConstants.FROM) String from);


    @GetMapping("/ys/job/getByCompId")
    Result<Integer> getByCompId(@RequestParam("compId") Integer compId, @RequestHeader(SecurityConstants.FROM) String from);


    @GetMapping("/ys/job/getListByCompId")
    Result<List<OvwYsjobRespDTO>> getListByCompId(@RequestParam("compId") Integer compId, @RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/ys/job/getJChenList")
	Result<List<OvwYsjobRespDTO>> getJChenList(@RequestHeader(SecurityConstants.FROM) String from);
}
