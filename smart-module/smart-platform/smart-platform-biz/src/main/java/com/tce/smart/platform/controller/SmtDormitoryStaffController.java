package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.DormitoryQueryNoStaffDTO;
import com.tce.smart.platform.api.dto.req.SelfLockPwdRefreshReqDTO;
import com.tce.smart.platform.api.dto.req.SelfLockPwdUpdateReqDTO;
import com.tce.smart.platform.api.dto.req.lock.*;
import com.tce.smart.platform.api.dto.resp.DormitoryRoomDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.SelfDormitoryRoomRespDTO;
import com.tce.smart.platform.api.dto.resp.DormitoryStaffRespDTO;
import com.tce.smart.platform.api.feign.RemoteSmartLockService;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.service.SmtDormitoryStaffService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.models.auth.In;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 员工宿舍信息表
 *
 * @author 齐佩
 * @date 2019-04-18 14:32:40
 */
@Api(tags = "住宿管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/dormitory/staff")
public class SmtDormitoryStaffController {
  private final  SmtDormitoryStaffService smtDormitoryStaffService;

  private final RemoteSmartLockService remoteSmartLockService;

	private final OpenApiAuthenticationAdapter openApiAuthenticationAdapter;

	/**
	 * App 服务调用内部住宿列表的 OAuth client_id。配置缺失时拒绝，避免猜测客户端标识。
	 */
	@Value("${security.inner.dormitory.app-client-id:}")
	private String appServiceClientId;

	/** App 读取本人住宿位置的受管用途，缺失或不一致时拒绝。 */
	@Value("${security.inner.dormitory.app-room-purpose:}")
	private String appRoomPurpose;

	/** 完整住宿详情只允许专用管理员服务客户端读取。 */
	@Value("${security.inner.dormitory.admin-room-detail-client-id:}")
	private String adminRoomDetailClientId;

	/** 完整住宿详情只允许专用管理员服务用途读取。 */
	@Value("${security.inner.dormitory.admin-room-detail-purpose:}")
	private String adminRoomDetailPurpose;

  /**
   * 分页查询员工内宿列表
   * @param page 分页对象
   * @param staffInDormitoryDTO 员工宿舍信息表
   * @return
   */
  @GetMapping("/page")
  public Result getSmtDormitoryStaffPage(Page page, StaffInDormitoryDTO staffInDormitoryDTO) {
    return new Result<>(smtDormitoryStaffService.getSmtDormitoryStaff(page,staffInDormitoryDTO));
  }

	@GetMapping("/edit/remark")
	public Result editSimpleRemark(@RequestParam("id") Integer id, @RequestParam("remark") String remark) {
		return new Result<>(smtDormitoryStaffService.editSimpleRemark(id,remark));
	}

	/**
	 * 删除已入住未报道员工
	 * @param ids
	 * @return
	 */
	@PostMapping("/delete/leave")
	public Result deleteNotRegister(@RequestBody DormitoryQueryNoStaffDTO ids) {
		return new Result<>(smtDormitoryStaffService.deleteNotRegister(ids.getIds()));
	}

  /**
   * 通过id查询员工宿舍信息表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result<>(smtDormitoryStaffService.getById(id));
  }

  /**
   * 新增员工宿舍信息表
   * @param dormitoryStaffDTO 员工宿舍信息表
   * @return Result
   */
  @SysLog("新增员工宿舍信息表  ")
  @PostMapping("addDormitoryStaff")
  public Result<Boolean> save(@RequestBody DormitoryStaffDTO dormitoryStaffDTO){
    return new Result<>(smtDormitoryStaffService.addDormitoryStaff(dormitoryStaffDTO));
  }

  @ApiOperation("批量导入员工入住信息记录")
  @PostMapping("/batch")
  public List<DormitoryStaffReqDTO> batch(@RequestBody List<DormitoryStaffReqDTO> dormitoryStaffDTOList) {
	  List<DormitoryStaffReqDTO> dormitoryStaffReqDTOS = smtDormitoryStaffService.batchAddDormitoryStaff(dormitoryStaffDTOList);
	  // 过滤出插入失败的记录
	  return dormitoryStaffReqDTOS.stream().filter(item -> StringUtils.isNotBlank(item.getMark())).collect(Collectors.toList());
  }

	@PostMapping("/info")
	@ApiOperation(value = "通过Excel批量更新员工入住及退宿信息")
	public ResponseEntity<byte[]> batchImportPersons(@RequestParam("dormId") Integer dormId,
													 @RequestParam("filename") MultipartFile multipartFile) {
		return this.smtDormitoryStaffService.batchImportPersons(dormId, multipartFile);
	}

  @SysLog("新增非员工宿舍信息表  ")
  @PostMapping("addDormitory")
  public Result addDormitory(@RequestBody DormitoryStaffDTO dormitoryStaffDTO){
    return smtDormitoryStaffService.addDormitory(dormitoryStaffDTO);
  }

  /**
   * 修改员工宿舍信息表
   * @param smtDormitoryStaff 员工宿舍信息表
   * @return Result
   */
  @SysLog("修改员工宿舍信息表  ")
  @PostMapping("updateDormitoryStaff")
  public Result updateById(@RequestBody UpdateDormitoryStaffDTO smtDormitoryStaff){
    return smtDormitoryStaffService.updateById(smtDormitoryStaff);
  }

  /**
   * 通过id删除员工宿舍信息表
   * @param id id
   * @return Result
   */
  @SysLog("删除员工宿舍信息表 ，离职 ")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return smtDormitoryStaffService.removeBedById(id);
  }


  @SysLog("修改员工住宿，离职，换宿，外宿 ")
  @PostMapping("changeDormitory")
  public Result changeDormitory(@RequestBody UpdateDormitoryStaffDTO smtDormitoryStaff){
    return smtDormitoryStaffService.changeDormitory(smtDormitoryStaff);
  }

	/**
	 * 申请内宿
	 * @param inDormitory
	 * @return
	 */
	@SysLog("申请内宿")
	@PostMapping("addInDormitory")
	public Result addInDormitory(@RequestBody InDormitoryDTO inDormitory) {
		return smtDormitoryStaffService.addInDormitory(inDormitory);
	}


	/**
	 * 查询今日入住
	 * @return
	 */
	@ApiOperation("查询今日入住")
	@PostMapping("/todayIn")
	public Result<IPage<DormitoryStaffRespDTO>> queryTodayIn(@ApiParam(name = "current",value = "当前页",required = true) @RequestParam Long current,
															 @ApiParam(name = "size",value = "大小",required = true) @RequestParam Long size) {
		return new Result<>(smtDormitoryStaffService.queryTodayIn(new Page(current,size)));
	}

	/**
	 * 换宿操作
	 * @return
	 */
	@ApiOperation("换宿操作")
	@PostMapping("/changeBed")
	public Result<Boolean> changeBed(@ApiParam(name = "staffBadge",value = "员工工号",required = true) @RequestParam String staffBadge,
															 @ApiParam(name = "bedId",value = "床位ID",required = true) @RequestParam Integer bedId,
									 @ApiParam(name = "oldBedId",value = "旧床位ID",required = true) @RequestParam Integer oldBedId) {
		return new Result<>(smtDormitoryStaffService.changeBed(staffBadge,bedId,oldBedId));
	}

	/**
	 * 退宿操作
	 * @param inId
	 * @return
	 */
	@ApiOperation("退宿操作")
	@PostMapping("/checkOutDormitory/{inId}")
	public Result<Boolean> checkOutDormitory(@ApiParam(name = "inId",value = "住宿记录ID",required = true) @PathVariable Integer inId){
		return new Result<>(smtDormitoryStaffService.checkOutDormitory(inId, null));
	}

	/**
	 * 管理员根据员工工号查询入住信息。
	 * 返回的园区必须属于当前管理员的数据权限，避免通过工号跨园区定位员工。
	 * @param staffBadge
	 * @return
	 */
	@ApiOperation("管理员根据员工工号查询入住信息")
	@GetMapping("/roomDetail/{staffBadge}")
	@PreAuthorize("@pms.hasPermission('platform_dormitory_staff_lookup')")
	public Result<DormitoryRoomDetailRespDTO> getStaffRoomInfoForAdmin(@ApiParam(name = "staffBadge",value = "员工工号",required = true) @PathVariable String staffBadge){
		SmartUser currentUser = currentAuthenticatedUser("未认证用户不可查询员工入住信息");
		DormitoryRoomDetailRespDTO roomDetail = smtDormitoryStaffService.getStaffRoomInfo(staffBadge);
		if (roomDetail != null && (roomDetail.getParkId() == null || currentUser.getParkIdList() == null
				|| !currentUser.getParkIdList().contains(roomDetail.getParkId()))) {
			throw new AccessDeniedException("无权查询该园区员工入住信息");
		}
		return new Result<>(roomDetail);
	}

	/**
	 * 仅供携带内部调用标识的 Feign 请求使用；外部流量不可使用该路由绕过园区数据权限。
	 */
	@Inner
	@OpenApi("server")
	@ApiOperation("内部根据员工工号查询入住信息")
	@GetMapping("/internal/roomDetail/{staffBadge}")
	public Result<DormitoryRoomDetailRespDTO> getStaffRoomInfoForInternal(
			@ApiParam(name = "staffBadge", value = "员工工号", required = true) @PathVariable String staffBadge,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose) {
		assertManagedInternalCaller(from, adminRoomDetailClientId, adminRoomDetailPurpose, purpose,
				"内部完整住宿详情调用未获授权");
		return new Result<>(smtDormitoryStaffService.getStaffRoomInfo(staffBadge));
	}

	/**
	 * 受管 App 服务读取指定员工的最小住宿位置投影。
	 * 调用方必须已在 App 服务侧把员工工号限制为当前认证主体。
	 */
	@Inner
	@OpenApi("server")
	@ApiOperation("内部查询员工本人最小入住信息")
	@GetMapping("/internal/self/roomDetail/{staffBadge}")
	public Result<SelfDormitoryRoomRespDTO> getSelfRoomDetailForInternal(
			@ApiParam(name = "staffBadge", value = "员工工号", required = true) @PathVariable String staffBadge,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose) {
		assertAppRoomCaller(from, purpose);
		return new Result<>(toSelfDormitoryRoom(smtDormitoryStaffService.getStaffRoomInfo(staffBadge)));
	}

	/**
	 * 仅供已取得服务令牌的内部调用读取员工入住列表，外部客户端不得访问或指定工号。
	 */
	@Inner
	@OpenApi("server")
	@ApiOperation("内部根据员工工号查询入住信息列表")
	@GetMapping("/internal/roomList/{staffBadge}")
	public Result<List<SelfDormitoryRoomRespDTO>> getStaffRoomInfoListForInternal(
			@ApiParam(name = "staffBadge",value = "员工工号",required = true) @PathVariable String staffBadge,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose){
		assertAppRoomCaller(from, purpose);
		List<DormitoryRoomDetailRespDTO> roomDetails = smtDormitoryStaffService.getStaffRoomInfoList(staffBadge);
		List<SelfDormitoryRoomRespDTO> rooms = (roomDetails == null ? new ArrayList<DormitoryRoomDetailRespDTO>() : roomDetails).stream()
				.map(this::toSelfDormitoryRoom).collect(Collectors.toList());
		return new Result<>(rooms);
	}

	@GetMapping("/lock/device/page")
	@ApiOperation(value = "智能锁分页查询")
	public Result devicePage(DevicePageReqDTO reqDTO) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		if (Objects.nonNull(reqDTO.getParkId())) {
			parkIdList = new ArrayList<>(1);
			parkIdList.add(reqDTO.getParkId());
		}
		reqDTO.setParkIds(parkIdList);
		return remoteSmartLockService.devicePage(reqDTO);
	}

	@PostMapping("/lock/bind/room")
	@ApiOperation(value = "智能锁绑定房间")
	public Result<Boolean> bindRoom(@RequestBody @Valid DeviceBindReqDTO reqDTO) {
		return remoteSmartLockService.bindRoom(reqDTO);
	}

	@GetMapping("/lock/open-door/record/page")
	@ApiOperation(value = "智能锁开门记录查询")
	public Result getOpenDoorRecordPage(@Valid OpenDoorRecordQueryDTO queryDTO) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		if (queryDTO.getParkId() != null) {
			parkIdList = new ArrayList<>();
			parkIdList.add(queryDTO.getParkId());
		}
		queryDTO.setParkIds(parkIdList);
		return remoteSmartLockService.getOpenDoorRecordPage(queryDTO);
	}

	@GetMapping("/lock/open-door/record/export")
	@ApiOperation(value = "开门记录导出Excel")
	public ResponseEntity<byte[]> exportOpenDoorRecord(OpenDoorRecordQueryDTO queryDTO) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		queryDTO.setParkIds(parkIdList);
		return remoteSmartLockService.exportOpenDoorRecord(queryDTO);
	}

	@GetMapping("/lock/support/key/{roomId}}")
	@ApiOperation(value = "根据房间id获得门锁支持的钥匙")
	public Result supportKeyByRoomId(@PathVariable("roomId") Long roomId) {
		return remoteSmartLockService.supportKeyByRoomId(roomId);
	}

	/**
	 * 分页查询
	 *
	 * @param reqDTO 设备权限表
	 * @return
	 */
	@GetMapping("/lock/permission/page")
	@ApiOperation("智能锁授权分页查询")
	public Result getLkDevicePermissionsPage(@Valid ApiDeviceAuthReqDTO reqDTO) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		if (reqDTO.getParkId() != null) {
			parkIdList = new ArrayList<>();
			parkIdList.add(reqDTO.getParkId());
		}
		reqDTO.setParkIds(parkIdList);
		return remoteSmartLockService.getLkDevicePermissionsPage(reqDTO);
	}

	/**
	 * 通过id查询设备权限表
	 *
	 * @param id id
	 * @return Result
	 */
	@GetMapping("/lock/permission/{id}")
	@ApiOperation("通过id查询设备权限")
	public Result getById(@PathVariable("id") Long id) {
		return remoteSmartLockService.getById(id);
	}

	/**
	 * 根据设备ID查找绑定的人员列表
	 *
	 * @param deviceId
	 * @return
	 */
	@GetMapping("/lock/permission/person/{deviceId}")
	@ApiOperation("根据设备ID查找绑定的人员列表")
	public Result getPersonsByDeviceId(@PathVariable("deviceId") Long deviceId) {
		return remoteSmartLockService.getPersonsByDeviceId(deviceId);
	}

	@PostMapping("/lock/permission/batch")
	@ApiOperation("批量授权")
	public Result<Boolean> addBatch(@RequestBody @Valid ApiBatchAuthDTO reqDTO) {
		return remoteSmartLockService.addBatch(reqDTO);
	}

	/**
	 * 修改设备权限表
	 *
	 * @param authDTO 设备权限表
	 * @return Result
	 */
	@PostMapping("/lock/permission/edit")
	@ApiOperation("编辑授权")
	public Result<Boolean> updateById(@RequestBody @Valid ApiEditAuthDTO authDTO) {
		return remoteSmartLockService.updateById(authDTO);
	}

	/**
	 * 重新授权
	 *
	 * @param authDTO 设备权限表
	 * @return Result
	 */
	@PostMapping("/lock/permission/reAuth")
	@ApiOperation("重新授权")
	public Result<Boolean> reAuth(@RequestBody @Valid ApiEditAuthDTO authDTO) {
		return remoteSmartLockService.reAuth(authDTO);
	}

	/**
	 * 通过id删除设备权限表
	 *
	 * @param id id
	 * @return Result
	 */
	@PostMapping("/lock/permission/del/{id}")
	@ApiOperation("删除授权")
	public Result<Boolean> removeById(@PathVariable("id") Long id) {
		return remoteSmartLockService.removeById(id);
	}

	/**
	 * 取消授权
	 *
	 * @param id
	 * @return
	 */
	@PostMapping("/lock/permission/cancelAuth/{id}")
	@ApiOperation("取消授权")
	public Result<Boolean> cancelAuth(@PathVariable Long id) {
		return remoteSmartLockService.cancelAuth(id);
	}

	/**
	 * 分页查询
	 *
	 * @param reqDTO  分页对象
	 * @return
	 */
	@GetMapping("/lock/task/record/page")
	public Result getTaskPage(@Valid ApiDeviceTaskQueryDTO reqDTO) {
		return remoteSmartLockService.getTaskPage(reqDTO);
	}

	/**
	 * 通过人员编号或姓名查询人员
	 *
	 * @param queryName queryName
	 * @return Result
	 */
	@GetMapping("/lock/person/search/{queryName}")
	@ApiOperation(value = "通过人员编号或姓名查询人员")
	public Result getByNumOrName(@PathVariable("queryName") @ApiParam("人员编号或姓名") String queryName) {
		return remoteSmartLockService.getByNumOrName(queryName);
	}

	/**
	 * 通过认证身份与人脸图核验获取本人动态码，不接受客户端指定工号。
	 */
	@ApiOperation("人脸比对获取本人动态码")
	@PostMapping("/me/face/compare")
	public Result<String> faceCompareForCurrentUser(@RequestBody @Valid SelfLockPwdRefreshReqDTO request) {
		return new Result<>(smtDormitoryStaffService.faceCompareForAuthenticatedStaff(currentAuthenticatedBadge(), request));
	}

	/**
	 * 读取当前认证员工的门锁动态码，工号不接受查询参数指定。
	 */
	@ApiOperation("获取本人门锁动态码")
	@GetMapping("/me/pwd")
	public Result<String> getPwdForCurrentUser() {
		return new Result<>(smtDormitoryStaffService.getPwdForAuthenticatedStaff(currentAuthenticatedBadge()));
	}

	/**
	 * 当前认证员工通过人脸核验刷新动态码。
	 */
	@ApiOperation("刷新本人门锁动态码")
	@PostMapping("/me/pwd")
	public Result<String> refreshPwdForCurrentUser(@RequestBody @Valid SelfLockPwdRefreshReqDTO request) {
		return new Result<>(smtDormitoryStaffService.refreshPwdForAuthenticatedStaff(currentAuthenticatedBadge(), request));
	}

	/**
	 * 当前认证员工修改门锁动态码。
	 */
	@ApiOperation("修改本人门锁动态码")
	@PostMapping("/me/lock/pwd")
	public Result<String> updateLockPwdForCurrentUser(@RequestBody @Valid SelfLockPwdUpdateReqDTO request) {
		return new Result<>(smtDormitoryStaffService.updateLockPwdForAuthenticatedStaff(
				currentAuthenticatedBadge(), request.getNewPwd()));
	}

	/**
	 * 当前认证员工的入住记录。路径不再携带工号，避免枚举他人入住信息。
	 */
	@ApiOperation("查询本人入住记录")
	@GetMapping("/me/roomList")
	public Result<List<SelfDormitoryRoomRespDTO>> getRoomListForCurrentUser() {
		List<DormitoryRoomDetailRespDTO> roomDetails = smtDormitoryStaffService.getStaffRoomInfoList(currentAuthenticatedBadge());
		List<SelfDormitoryRoomRespDTO> rooms = (roomDetails == null ? new ArrayList<DormitoryRoomDetailRespDTO>() : roomDetails).stream()
				.map(this::toSelfDormitoryRoom).collect(Collectors.toList());
		return new Result<>(rooms);
	}

	private String currentAuthenticatedBadge() {
		return currentAuthenticatedUser("未认证用户不可操作门锁动态码").getUsername();
	}

	private SmartUser currentAuthenticatedUser(String denialMessage) {
		Authentication authentication = SecurityUtils.getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new AccessDeniedException(denialMessage);
		}
		SmartUser currentUser = SecurityUtils.getUser(authentication);
		if (currentUser == null || StringUtils.isEmpty(currentUser.getUsername())) {
			throw new AccessDeniedException(denialMessage);
		}
		return currentUser;
	}

	/**
	 * 本人住宿位置属于员工位置资料：仅受管 App client_credentials 加配置中心受管用途可读取。
	 * 即使请求伪造 from=Y 或持有通用 server scope 令牌也必须拒绝。
	 */
	private void assertAppRoomCaller(String from, String purpose) {
		assertManagedInternalCaller(from, appServiceClientId, appRoomPurpose, purpose, "内部本人住宿调用未获授权");
	}

	/**
	 * 对内部敏感资料执行客户端与用途双重精确收口；任一配置缺失即失败关闭。
	 */
	private void assertManagedInternalCaller(String from, String expectedClientId, String expectedPurpose, String purpose,
			String denialMessage) {
		Authentication authentication = SecurityUtils.getAuthentication();
		if (!SecurityConstants.FROM_IN.equals(from) || StringUtils.isEmpty(expectedClientId)
				|| StringUtils.isEmpty(expectedPurpose) || !expectedPurpose.equals(purpose) || authentication == null
				|| !openApiAuthenticationAdapter.isClientOnly(authentication)
				|| !expectedClientId.equals(openApiAuthenticationAdapter.clientId(authentication))) {
			throw new AccessDeniedException(denialMessage);
		}
	}

	/** 将通用住宿详情映射为本人可见的最小位置投影，禁止向 App 透传工号或门锁动态码。 */
	private SelfDormitoryRoomRespDTO toSelfDormitoryRoom(DormitoryRoomDetailRespDTO room) {
		if (room == null) {
			return null;
		}
		return SelfDormitoryRoomRespDTO.builder().id(room.getId()).bedNumber(room.getBedNumber())
				.parkId(room.getParkId()).parkName(room.getParkName()).dormitoryId(room.getDormitoryId())
				.dormitoryName(room.getDormitoryName()).floorId(room.getFloorId()).floorName(room.getFloorName())
				.roomId(room.getRoomId()).roomName(room.getRoomName()).inRecordId(room.getInRecordId()).build();
	}

	@ApiOperation("修改入住备注")
	@GetMapping("/update/remark")
	public Result<Boolean> updateSimpleRemark(@RequestParam("id") String id, @RequestParam("remark") String remark){
		return Result.success(smtDormitoryStaffService.updateSimpleRemark(Long.parseLong(id), remark));
	}
}
