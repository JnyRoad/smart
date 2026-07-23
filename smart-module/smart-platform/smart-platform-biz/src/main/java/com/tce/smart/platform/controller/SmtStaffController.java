package com.tce.smart.platform.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.SmtVehicleRespDTO;
import com.tce.smart.platform.api.dto.req.EmpHrReqDTO;
import com.tce.smart.platform.api.dto.req.AdminStaffPhoneUpdateReqDTO;
import com.tce.smart.platform.api.dto.req.AdminStaffPageQueryReqDTO;
import com.tce.smart.platform.api.dto.req.AdminStaffUpdateReqDTO;
import com.tce.smart.platform.api.dto.req.AdminTemporaryStaffQueryReqDTO;
import com.tce.smart.platform.api.dto.req.TempStaffEditReqDTO;
import com.tce.smart.platform.api.dto.req.TempStaffUpdateStatusReqDTO;
import com.tce.smart.platform.api.dto.resp.*;
import com.tce.smart.platform.core.ao.SmtAppStaffAuthBatchSaveAO;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtVehicle;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.vo.EmpHrVO;
import com.tce.smart.platform.core.vo.ToC6ePhoto;
import com.tce.smart.platform.core.vo.VehicleParkDetailVO;
import com.tce.smart.platform.service.*;
import com.tce.smart.tool.constant.VehicleConstants;
import com.tce.smart.tool.enums.StaffStatusEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 员工表
 *
 * @author 齐佩
 * @date 2019-04-13 18:18:42
 */
@RestController
@AllArgsConstructor
@RequestMapping("/staff")
@Api(tags = "platform-员工管理")
public class SmtStaffController extends BaseController {

	private final SmtStaffService smtStaffService;

	private final SmtVehicleService smtVehicleService;

	private final SmtAppStaffAuthService smtAppStaffAuthService;

	private final SmtImageService smtImageService;

	private final SmtDormitoryStaffService dormitoryStaffService;

	private final SmtStaffExtService smtStaffExtService;

	/**
	 * 后台按认证主体园区范围分页查询员工最小信息。
	 *
	 * 原 /page 接口直接返回含手机号、证件号和人脸文件标识的 StaffListVO，已删除。
	 * 所有后台列表消费者必须迁移至本受权限保护且显式投影的契约。
	 */
	@ApiOperation("后台员工最小列表")
	@PostMapping("/admin/page")
	@PreAuthorize("@pms.hasPermission('platform_staff_lookup')")
	public Result adminStaffPage(Page page, @RequestBody AdminStaffPageQueryReqDTO request) {
		return success(smtStaffService.getAdminStaffPage(page, request,
				currentAuthenticatedUser().getParkIdList()));
	}

	/**
	 * 查询未分配宿舍在职员工
	 * @param smtStaff
	 * @return
	 */
	@GetMapping("/quetyStaffNODormitory")
	public Result getSmtNoStaffPage(Page page,SearchStaffDTO smtStaff) {
		return new Result<>(smtStaffService.quetyStaffNODormitory( page,smtStaff));
	}

	/**
	 * 新增员工表
	 *
	 * @param smtStaff
	 *            员工表
	 * @return Result
	 */
	@SysLog("新增员工表")
	@PostMapping("addStaff")
	public Result save(@RequestBody ApplicationStaffDTO smtStaff) {
		return smtStaffService.addStaff(smtStaff);
	}

	/**
	 * 新增员工表
	 *
	 * @param smtStaff
	 *            员工表
	 * @return Result
	 */
	@SysLog("导入临时员工")
	@ApiOperation("导入临时员工")
	@PostMapping("/addBatchTempStaff")
	public Result saveBatchTemp(@RequestBody List<TempStaffEditReqDTO> smtStaff) {
		return success(smtStaffExtService.saveBatchTemporaryStaff(smtStaff, dormitoryStaffService));
	}

	@PostMapping("/deleteBatchTempStaff")
	public Result deleteBatchTempStaff(@RequestBody TempStaffEditReqDTO ids) {
		for(String id: ids.getIds()){
			SmtStaff smtStaff = smtStaffService.getById(Long.valueOf(id));
			smtStaff.setStatus(StaffStatusEnum.STAFF_STATUS_QUIT.getCode());
			smtStaffService.synDeleteUserInfo(smtStaff);
			smtStaff.updateById();
		}
		return success(Boolean.TRUE);
	}

	/**
	 * 后台按工号查询员工最小信息。
	 *
	 * 查询范围由当前认证用户的园区权限决定，响应不包含任何个人敏感信息。
	 */
	@ApiOperation("后台按工号查询员工最小信息")
	@GetMapping("/lookup")
	@PreAuthorize("@pms.hasPermission('platform_staff_lookup')")
	public Result<List<StaffLookupRespDTO>> lookup(@RequestParam("badge") String badge) {
		return success(smtStaffService.searchStaffForAdmin(badge, currentAuthenticatedUser().getParkIdList()));
	}

	/**
	 * 后台按员工主键查询经园区范围过滤的最小详情。
	 *
	 * 该接口不能替代敏感人员档案查询：响应只包含当前 Smart UI 人员识别与组织展示
	 * 实际需要的非敏感字段，认证主体也不能越过其已授权园区。
	 */
	@ApiOperation("后台查询员工受控详情")
	@GetMapping("/admin/{staffId}")
	@PreAuthorize("@pms.hasPermission('platform_staff_lookup')")
	public Result<AdminStaffDetailRespDTO> adminStaffDetail(@PathVariable("staffId") Long staffId) {
		return success(smtStaffService.getAdminStaffDetail(staffId, currentAuthenticatedUser().getParkIdList()));
	}

	/**
	 * 查询当前认证员工的入住资料摘要。
	 *
	 * 工号始终从认证主体获取，外部请求参数不能指定其他员工。
	 */
	@ApiOperation("查询本人入住资料摘要")
	@GetMapping("/me/check-in-profile")
	public Result<StaffSelfCheckInProfileRespDTO> myCheckInProfile() {
		SmartUser currentUser = currentAuthenticatedUser();
		return success(smtStaffService.getCheckInProfileForBadge(currentUser.getUsername()));
	}

	/** 只接受网关解析出的 SmartUser，缺失认证主体时 fail-closed。 */
	private SmartUser currentAuthenticatedUser() {
		Authentication authentication = SecurityUtils.getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new AccessDeniedException("未认证用户不可查询员工资料");
		}
		SmartUser currentUser = SecurityUtils.getUser(authentication);
		if (currentUser == null) {
			throw new AccessDeniedException("未认证用户不可查询员工资料");
		}
		return currentUser;
	}

	@SysLog("后台临时人员最小列表")
	@ApiOperation("后台临时人员最小列表")
	@PostMapping("/admin/temporary/page")
	@PreAuthorize("@pms.hasPermission('platform_staff_lookup')")
	public Result temporaryStaffPage(Page page, @RequestBody AdminTemporaryStaffQueryReqDTO request) {
		return success(smtStaffService.getTemporaryStaffPageForAdmin(page, request,
				currentAuthenticatedUser().getParkIdList()));
	}

	@SysLog("后台临时人员最小详情")
	@ApiOperation("后台临时人员最小详情")
	@GetMapping("/admin/temporary/{staffId}")
	@PreAuthorize("@pms.hasPermission('platform_staff_lookup')")
	public Result temporaryStaffDetail(@PathVariable("staffId") Long staffId) {
		return success(smtStaffService.getTemporaryStaffDetailForAdmin(staffId,
				currentAuthenticatedUser().getParkIdList()));
	}

	@SysLog("新增临时员工")
	@ApiOperation("新增临时员工")
	@PostMapping("/addTempStaff")
	public Result saveTemp(@RequestBody TempStaffEditReqDTO smtStaff) {
		return success(smtStaffService.saveTemporaryStaff(smtStaff, dormitoryStaffService));
	}

	@SysLog("修改临时员工")
	@ApiOperation("修改临时员工")
	@PostMapping("/updateTempStaff")
	public Result updateTemp(@RequestBody TempStaffEditReqDTO smtStaff) {
		return success(smtStaffService.updateTemporaryStaff(smtStaff));
	}

	@SysLog("删除临时员工")
	@ApiOperation("删除临时员工")
	@PostMapping("/deleteTempStaff")
	public Result deleteTempStaff(@RequestParam("staffId") Long staffId) {
		SmtStaff smtStaff = smtStaffService.getById(staffId);
		smtStaff.setStatus(StaffStatusEnum.STAFF_STATUS_QUIT.getCode());
		smtStaffService.synDeleteUserInfo(smtStaff);
		return success(smtStaffService.removeById(staffId));
	}

	@ApiOperation("后台修改员工手机号")
	@PostMapping("/admin/update-phone")
	@PreAuthorize("@pms.hasPermission('platform_staff_manage')")
	public Result<Boolean> updateStaffPhone(@RequestBody AdminStaffPhoneUpdateReqDTO request) {
		return success(smtStaffService.updateStaffPhoneForAdmin(request, currentAuthenticatedUser().getParkIdList()));
	}

	@SysLog("新增员工到hr中间表")
	@PostMapping("addStaffToHR")
	public Result addStaffToHR(@RequestBody ApplicationStaffDTO smtStaff) {
		return smtStaffService.addStaffToHR(smtStaff);
	}
	/**
	 * 修改员工表
	 *
	 * @param smtStaff
	 *            员工表
	 * @return Result
	 */
	@SysLog("后台修改员工基础资料")
	@PostMapping("/admin/update")
	@PreAuthorize("@pms.hasPermission('platform_staff_manage')")
	public Result<Boolean> updateStaff(@RequestBody AdminStaffUpdateReqDTO request) {
		return success(smtStaffService.updateStaffForAdmin(request, currentAuthenticatedUser().getParkIdList()));
	}



	/**
	 * 员工，我的宿舍
	 */
	@SysLog("我的宿舍")
	@PostMapping("myDormitory")
	public Result<MyDormitoryRespDTO> myDormitory(@RequestBody SmtStaff smtStaff) {
		return success(smtStaffService.myDormitory(smtStaff), MyDormitoryRespDTO.class);
	}


	/**
	 * 从视图中同步员工信息
	 * @param empHr
	 * @return
	 */
	@Inner
	@PostMapping("/sync")
	public Result syncStaff(@RequestBody EmpHrVO empHr) {
		smtStaffService.syncStaff(empHr, dormitoryStaffService);
		return success();
	}

	@Inner
	@PostMapping("/sync/img")
	public Result syncStaffImg() {
		smtStaffService.syncFaceImg();
		return success();
	}


	/**
	 * 后台批量离职前查询当前管理员园区内的临时员工。
	 *
	 * 调用方不能提交或伪造 BU 编号；园区范围始终从已认证管理主体取得。
	 */
	@ApiOperation("后台批量查询临时员工最小资料")
	@GetMapping("/admin/temporary/by-badges")
	@PreAuthorize("@pms.hasPermission('platform_staff_lookup')")
	public Result<List<AdminTemporaryStaffRespDTO>> temporaryStaffByBadges(@RequestParam("badges") String badges) {
		List<String> badgeList = Arrays.stream(badges.split(","))
				.map(String::trim)
				.filter(StrUtil::isNotBlank)
				.collect(java.util.stream.Collectors.toList());
		return success(smtStaffService.searchTemporaryStaffForAdmin(badgeList, currentAuthenticatedUser().getParkIdList()));
	}

	/**
	 *
	 * 我的车辆，查询
	 * @param badge 员工badge
	 * @return
	 */
	@SysLog("app端通过员工号查询员工车辆")
	@SuppressWarnings("rawtypes")
	@Inner
	@GetMapping("/myVehicle")
	public Result<IPage<SmtVehicleRespDTO>> getMyVehicle(Page page, @RequestParam("badge") String badge) {
		return success(smtStaffService.getMyVehicle(page,badge), SmtVehicleRespDTO.class);
	}


	/**
	 * 车辆入园申请
	 * @param applyVehicleDTO
	 * @return
	 */
	@SysLog("app端添加车辆入园申请")
	@Inner
	@PostMapping("addVehiclePark")
	public Result addVehiclePark(@RequestBody ApplyAuthDTO applyVehicleDTO) {
		return smtStaffService.addVehiclePark(applyVehicleDTO);
	}

	/**
	 * 车辆权限列表查询
	 * @param plateNumber
	 * @return
	 */
	@SysLog("app端通过车牌号查询车辆可以进出的园区")
	@Inner
	@GetMapping("/getVehiclePark")
	public Result<List<VehicleApplyRespDTO>> getVehiclePark(@RequestParam("plateNumber") String plateNumber) {
		return success(smtStaffService.getVehiclePark(plateNumber),VehicleApplyRespDTO.class);
	}

	/**
	 * 根据权限id查询园区权限的详情
	 * @param id  申请入园表主键
	 * @return
	 */
	@SysLog("app通过入园权限id，查看车辆信息")
	@Inner
	@GetMapping("/getVehicleParkById/{id}")
	public Result<SmtVehicleRespDTO> getVehicleParkById(@PathVariable("id") Integer id) {
		VehicleParkDetailVO smtVehicle = smtStaffService.getVehicleParkById(id);
		SmtVehicleRespDTO smtVehicleRespDTO = new SmtVehicleRespDTO();
		BeanUtils.copyProperties(smtVehicle, smtVehicleRespDTO);
		return success(smtVehicleRespDTO);
	}

	@SysLog("app调用接口添加车辆")
	@Inner
	@PostMapping("addVehicle")
	public Result addVehicle(@RequestBody AddVehicleDTO addVehicleDTO) {
		return smtStaffService.addVehicle(addVehicleDTO);
	}

	@SysLog("app调用接口移除车辆")
	@Inner
	@GetMapping("delVehicle")
	public Result delVehicle(@RequestParam("plateNumber") String plateNumber) {
		SmtVehicle vehicle = smtVehicleService.getOne(Wrappers.<SmtVehicle>query().lambda().eq(SmtVehicle::getVehiclePlate, StrUtil.removeAll(plateNumber, " ").toUpperCase())
				.eq(SmtVehicle::getIsDelete, VehicleConstants.UNDELETED));
		return new Result<>(smtVehicleService.deleteVehicle(vehicle.getId(),null));
	}

	@SysLog("app调用接口获取员工二维码")
	@Inner
	@GetMapping("/qrcode")
	public Result getQrcode(String badge) {
		return smtStaffService.getQrcode(badge);
	}

	@SysLog("app调用接口是否需要完善信息")
	@Inner
	@GetMapping("/perfect/check")
	public Result<Boolean> checkPerfectInfo(@RequestParam("badge") String badge) {
		return new Result<Boolean>(smtStaffService.checkPerfectInfo(badge));
	}

	@SysLog("app调用接口完善人脸信息")
	@Inner
	@PostMapping("/perfect/info/face")
	public Result<Boolean>  perfectFace(@RequestBody StaffPerfectDTO perfectDTO) {
		return smtStaffService.perfectFace(perfectDTO);
	}

	/**
	 * 批量分配员工权限
	 *
	 * @param appStaffAuthBatchaveAO app员工权限批量新增修改Ao
	 * @return Result
	 */
	@SysLog("修改")
	@PostMapping("/auth/app/update")
	public Result updateAuthStaff(@RequestBody @Valid SmtAppStaffAuthBatchSaveAO appStaffAuthBatchaveAO) {
		return success(smtAppStaffAuthService.batchUpdateStaffAuth(appStaffAuthBatchaveAO));
	}

	/**
	 * 获取用户已分配的App模块ID
	 * @return
	 */
	@GetMapping("/auth/app/module/list")
	public Result<List<String>> getStaffAppModule(@RequestParam("badge") String badge){
		return new Result<List<String>>(smtAppStaffAuthService.getStaffModule(badge));
	}

	/**
	 * 登陆初始化员工权限
	 * @return
	 */
	@PostMapping("/auth/login/init")
	public Result<Boolean> inintLoginAuth(@RequestParam("badge") String badge){
		return new Result<Boolean>(smtAppStaffAuthService.initLoginAuth(badge));
	}

	/**
	 * 人脸对比
	 * @param staffPerfectDTO
	 * @return
	 */
	@PostMapping("/face/compare")
	public Result<Boolean> faceSearchForCompare(@RequestBody StaffPerfectDTO staffPerfectDTO){
		return success(smtStaffService.faceSearchForCompare(staffPerfectDTO));
	}

	/**
	 * 同步员工头像到C6
	 *
	 * @param badge 员工工号
	 * @return Result<Boolean>
	 */
	@PostMapping("/sync/face/login/{badge}")
	public Result<Boolean> synStaffFaceImage(@PathVariable("badge") String badge) {
		return new Result<Boolean>(smtStaffService.synStaffFaceImage(badge));
	}

	@Inner
	@ApiOperation("同步员工人员和人脸到ISC")
	@PostMapping("/isc/person/face/sync")
	public Result<Boolean> syncIscPersonFace(@RequestParam("badge") String badge,
											 @RequestParam("parkId") Integer parkId,
											 @RequestParam(value = "imageId", required = false) String imageId) {
		return success(smtStaffService.syncIscPersonFace(badge, parkId, imageId));
	}

	@Inner
	@ApiOperation("重试ISC员工人员和人脸同步失败任务")
	@PostMapping("/isc/person/face/retry")
	public Result<Boolean> retryFailedIscPersonFaceSync() {
		return success(smtStaffService.retryFailedIscPersonFaceSync());
	}

	@SysLog("员工上传图片批量审核")
	@PostMapping("/check/facePic")
	public Result checkFacePic(@RequestBody CheckFacePicDTO check) {
		return success(smtStaffService.checkFacePic(check));
	}


	@SysLog("员工上传图片")
	@PostMapping("/upload/facePic")
	public Result<String> upload(@RequestBody CheckFacePicDTO check) {
		String upload = smtStaffService.upload(check);
		return success(upload);
	}

	@SysLog("给c6提供查看员工的图片")
	@PostMapping("/toC6ePhoto")
	public Result<ToC6ePhoto> toC6ePhoto(@RequestBody ToC6ePhoto toC6ePhoto) {
		return new Result<ToC6ePhoto>(smtStaffService.toC6ePhoto(toC6ePhoto));
	}

	@SysLog("新员工注册")
	@PostMapping("/register")
	public Result<Boolean> register(@RequestBody StaffRegisterDTO staffRegisterDTO) {
		return new Result<Boolean>(smtStaffService.register(staffRegisterDTO));
	}


	/**
	 * 申请内宿
	 * @param apply
	 * @return
	 */
	@SysLog("查看权限策略详情")
	@GetMapping("getAuthInfo/{id}")
	public Result getAuthInfo(@PathVariable String id) {
		return smtStaffService.getAuthInfo(id);
	}

	/**
	 * 查询入职的员工
	 * @param page
	 * @param smtStaff
	 * @return
	 */
	@GetMapping("/toStaff/page")
	public Result getToStaffPage(Page page, SearchToStaffDTO searchToStaffDTO) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		searchToStaffDTO.setParkIds(parkIds);
		return new Result<>(smtStaffService.getTOStaffPage(page,searchToStaffDTO));
	}
	/**
	 * 查询入职人员的详情
	 * @param id
	 * @return
	 */
	@GetMapping("/toStaff/{id}")
	public Result getToStaffInfoById(@PathVariable("id") String id) {
		return smtStaffService.getToStaffInfoById(id);
	}

	/**
	 * 根据员工号查询该员工所属园区
	 * @param staffBadge
	 * @return
	 */
	@GetMapping("/getStaffPark/{staffBadge}")
	public Result<List<SmtParkRespDTO>> getStaffPark(@PathVariable("staffBadge") String staffBadge) {
		return success(smtStaffService.getStaffPark(staffBadge), SmtParkRespDTO.class);
	}

	/**
	 * 根据员工号模糊查询该员工信息
	 * @param staffBadge
	 * @return
	 */
	@GetMapping("/getStaffList/{staffBadge}")
	public Result<List<StaffPartInfo>> getStaffList(@PathVariable("staffBadge") String staffBadge) {
		Assert.notEmpty(staffBadge,"查询工号不能为NULL");
		return success(smtStaffService.getStaffInfo(staffBadge));
	}

	/**
	 * 员工同步分页查询
	 *
	 * @param page 分页对象
	 * @return
	 */
	@GetMapping("/sync/list")
	public Result<IPage<EmpHrReqDTO>> getSmtStaffList(Page page) {
		return new Result<>(smtStaffService.getStaffList(page));
	}

	/**
	 * 根据员工号获取卡片
	 * @param empNo
	 * @return
	 */
	@GetMapping("/getEmpCard/{empNo}")
	public Result<String> getEmpCard(@PathVariable("empNo") String empNo) {
		return success(smtStaffService.getEmpCard(empNo));
	}

	/**
	 * 添加设备任务
	 * @return
	 */
	@Inner
	@PostMapping("/device/task/{action}")
	public Result<Boolean> addDeviceTask(@RequestBody SmtStaff staff, @PathVariable("action") Integer action) {
		smtStaffService.addDeviceTask(staff, action);
		return success(true);
	}


	@SysLog("临时员工离职改为在职")
	@ApiOperation("临时员工离职改为在职")
	@PostMapping("/update/temp/status")
	public Result updateTempStatus(@RequestBody TempStaffUpdateStatusReqDTO ids) {
		return success(smtStaffService.batchUpdateTempStatus(ids.getIds()));
	}

}
