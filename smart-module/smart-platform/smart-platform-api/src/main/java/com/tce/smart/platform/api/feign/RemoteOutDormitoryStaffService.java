package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.SmtOutDormitoryStaffDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 员工外宿申请
 * @author qipei
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteOutDormitoryStaffService {

	@GetMapping("/out/dormitory/staff/status")
	Result status(@RequestParam("staffBadge") String staffBadge, @RequestHeader(SecurityConstants.FROM) String from);
	/**
	 * 申请外宿
	 * @param apply
	 * @return
	 */
	@PostMapping("/out/dormitory/staff/addOutDormitory")
	Result addOutDormitory(@RequestBody SmtOutDormitoryStaffDTO apply, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 获取补贴信息
	 * @param staffBadge
	 * @return
	 */
	@GetMapping("/out/dormitory/staff/getAllowance")
	Result getAllowance(@RequestParam("staffBadge") String staffBadge,@RequestParam("type") Integer type, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 获取外宿信息
	 * @param staffBadge
	 * @return
	 */
	@GetMapping("/out/dormitory/staff/getOutDormitoryInfo")
	Result<List<SmtOutDormitoryStaffDTO>> getOutDormitoryInfo(@RequestParam("staffBadge") String staffBadge, @RequestParam("type") Integer type, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 获取外宿审批详情
	 * @param id
	 * @param fromIn
	 * @return
	 */
	@GetMapping("/out/dormitory/staff/detail")
	Result outRoomApplyDetail(@RequestParam("id") Integer id, @RequestHeader(SecurityConstants.FROM) String fromIn);

	/**
	 * 清理撤销外宿审批的外宿记录
	 * @param fromIn
	 */
	@GetMapping("/out/dormitory/staff/refresh")
	void refreshOutDormitory(@RequestHeader(SecurityConstants.FROM) String fromIn,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);


	//查询外宿补贴开始时间的配置时间
	@GetMapping("/out/dormitory/staff/getDormitroySet")
	Result<Integer> getDormitroySet(@RequestHeader(SecurityConstants.FROM) String fromIn);

	/**
	 * 查询员工外宿列表
	 * @param current
	 * @param size
	 * @param staffBadge
	 * @param fromIn
	 * @return
	 */
	@GetMapping("/out/dormitory/staff/page/list")
	Result getOutDormitoryPageList(@RequestParam("current") final Integer current, @RequestParam("size") final Integer size, @RequestParam("staffBadge") final String staffBadge,@RequestHeader(SecurityConstants.FROM) String fromIn);

	 @GetMapping("/out/dormitory/staff/detail/{id}")
	 Result getOutDormitoryDetailById(@RequestParam("id") final Integer id,@RequestHeader(SecurityConstants.FROM) String fromIn);

	 @GetMapping("/out/dormitory/staff/detail/byId")
	Result outRoomDetailById(@RequestParam("recordId") String recordId, @RequestHeader(SecurityConstants.FROM) String fromIn);
}
