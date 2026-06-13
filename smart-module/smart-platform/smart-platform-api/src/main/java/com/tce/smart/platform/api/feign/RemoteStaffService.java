package com.tce.smart.platform.api.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.SmtStaffDTO;
import com.tce.smart.platform.api.dto.req.*;
import com.tce.smart.platform.api.dto.resp.MyDormitoryRespDTO;
import com.tce.smart.platform.api.dto.resp.StaffInfoRespDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 员工
 * @author 齐佩
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
@Service
public interface RemoteStaffService {

	/**
	 * 根据员员工工号获取员工基本信息
	 * @param badge
	 * @return
	 */
	@GetMapping("/staff/baseInfo/{badge}")
	Result<StaffInfoRespDTO> getBaseinfoByBadge(@RequestParam("badge") String badge, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 根据员工ID查询员工实体信息
	 *
	 * @param staffId 员工ID
	 * @param from 调用来源
	 *
	 * @return 员工信息
	 */
	@GetMapping("/staff/simple/get")
	Result<SmtStaffDTO> getSimpleSttaffById(@RequestParam("staffId") String staffId, @RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/staff/simple/get-list")
	Result<List<SmtStaffDTO>> getSimpleSttaffListByIds(@RequestParam("staffIds") ArrayList<String> staffIds, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 根据员工工号查询员工实体信息
	 *
	 * @param badge 员工工号
	 * @return 员工信息
	 */
	@GetMapping("/staff/simple/get/badge")
	Result<SmtStaffDTO> getSimpleSttaffByBadge(@RequestParam("badge") String badge);

	/**
	 * 根据员工工号获取员工的详细信息
	 * @param badge
	 * @return
	 */
	@GetMapping("/staff/getFullByBadge/{badge}")
	Result<StaffInfoRespDTO> getFullByBadge(@RequestParam("badge") String badge, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 同步员工信息
	 * @param empHr empHr
	 * @param from from
	 * @return
	 */
	@PostMapping("/staff/sync")
	Result syncStaff(@RequestBody EmpHrReqDTO empHr, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 同步员工信息
	 * @param from from
	 * @return
	 */
	@PostMapping("/staff/sync/img")
	Result syncStaffImg(@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 修改我的紧急联系人
	 *
	 * @param EmergencyDTO
	 * @return
	 */
	@PostMapping("/staff/emergency/updateByBadge")
	Result updateByBadge(@RequestBody StaffEmergencyReqDTO EmergencyDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 我的宿舍
	 * @param smtStaff
	 * @return
	 */
	@PostMapping("/staff/myDormitory")
	Result<MyDormitoryRespDTO> myDormitory(@RequestBody SmtStaffDTO smtStaff, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 获取员工二维码
	 * @param badge
	 * @return
	 */
	@GetMapping("/staff/qrcode")
	Result getQrcode(@RequestParam("badge") String badge, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 员工申请内宿
	 * @param inDormitoryReqDTO inDormitoryReqDTO
	 * @param from from
	 * @return
	 */
	@PostMapping("/dormitory/staff/addInDormitory")
	Result addInDormitory(@RequestBody InDormitoryReqDTO inDormitoryReqDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * app调用接口是否需要完善信息
	 *
	 * @param badge 员工工号
	 *
	 * @return
	 */
	@GetMapping("/staff/perfect/check")
	Result<Boolean> checkPerfectInfo(@RequestParam("badge") String badge);

	/**
	 * app调用接口完善人脸信息
	 *
	 * @param staffPerfectReqDTO staffPerfectReqDTO
	 * @return
	 */
	@PostMapping("/staff/perfect/info/face")
	Result<Boolean> perfectFace(@RequestBody StaffPerfectReqDTO staffPerfectReqDTO);

	/**
	 * 获取用户已分配的App模块ID
	 *
	 * @param badge 员工好
	 * @return
	 */
	@GetMapping("/staff/auth/app/module/list")
	Result<List<String>> getStaffAppModule(@RequestParam("badge") String badge);

	/**
	 * 登陆初始化员工权限
	 * @return
	 */
	@PostMapping("/staff/auth/login/init")
	Result<Boolean> inintLoginAuth(@RequestParam("badge") String badge);

	/**
	 * 人脸登陆-人脸搜索
	 *
	 * @return
	 */
	@PostMapping("/staff/face/search/login")
	Result<SmtStaffDTO> faceSearchForLogin(@RequestBody StaffPerfectReqDTO staffPerfectDTO);


	/**
	 * 修改员工手机号
	 * @param smtStaff
	 * @return
	 */
	@PostMapping("/staff/updatePhone")
	Result<Boolean> updatePhone(@RequestBody SmtStaffDTO smtStaff);

	/**
	 * 同步员工头像到C6
	 * @param badge 员工工号
	 */
	@PostMapping("/staff/sync/face/login/{badge}")
	Result<Boolean> synStaffFaceImage(@PathVariable("badge") String badge);

	/**
	 * 同步员工人员和人脸到ISC
	 */
	@PostMapping("/staff/isc/person/face/sync")
	Result<Boolean> syncIscPersonFace(@RequestParam("badge") String badge,
									  @RequestParam("parkId") Integer parkId,
									  @RequestParam(value = "imageId", required = false) String imageId,
									  @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 重试ISC员工人员和人脸同步失败任务
	 */
	@PostMapping("/staff/isc/person/face/retry")
	Result<Boolean> retryFailedIscPersonFaceSync(@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 根据员工号查询空余床位
	 * @param badge
	 * @param from
	 * @return
	 */
	@GetMapping("/dormitory/room/getJche/freeBed")
	Result getJcheFreeBed(@RequestParam("parkId") Integer parkId, @RequestParam("badge") String badge,@RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 根据员工号查询该员工所属园区
	 * @param staffBadge
	 * @return
	 */
	@GetMapping("/staff/getStaffPark/{staffBadge}")
	Result getStaffPark(@PathVariable("staffBadge") String staffBadge,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 *
	 * @param page
	 * @param from
	 * @return
	 */
	@GetMapping("/staff/sync/list")
	Result<Page<EmpHrReqDTO>> getStaffList(@RequestParam("current") Long current, @RequestParam("size") Long size, @RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/staff/query/{mobile}")
	Result<List<SmtStaffDTO>> queryMobile(@PathVariable("mobile") String mobile, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 远程添加设备任务
	 * @param smtStaff
	 * @return
	 */
	@PostMapping("/staff/device/task/{action}")
	Result<Boolean> addDeviceTask(@RequestBody SmtStaffDTO smtStaff, @PathVariable("action") Integer action);

}
