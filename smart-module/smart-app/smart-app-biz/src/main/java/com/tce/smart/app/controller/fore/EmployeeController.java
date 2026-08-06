package com.tce.smart.app.controller.fore;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.ao.EmployeeNoteAo;
import com.tce.smart.app.ao.fore.EmployeeUpdateAo;
import com.tce.smart.app.ao.fore.PerfectInfoAo;
import com.tce.smart.app.ao.fore.RoomApplyAo;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppEmployeeNoteService;
import com.tce.smart.app.service.fore.EmployeeService;
import com.tce.smart.app.vo.fore.EmployeeSalayType;
import com.tce.smart.app.vo.fore.NewEmployeeNoteDetailVo;
import com.tce.smart.app.vo.fore.NewEmployeeNoteListVo;
import com.tce.smart.app.vo.wechat.RelationTypeVO;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.SmtOutDormitoryStaffDTO;
import com.tce.smart.platform.api.feign.RemoteOutDormitoryStaffService;
import com.tce.smart.tool.exception.TCEException;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 员工信息控制器
 *
 * @author Administrator
 */
@RestController
@AllArgsConstructor
@RequestMapping("/employee")
public class EmployeeController extends BaseController {

	private final EmployeeService service;

	private final AppEmployeeNoteService appEmployeeNoteService;

	private final RemoteOutDormitoryStaffService remoteOrtDormitoryStaffService;

	@GetMapping("/allowance/status")
	public Result status(@RequestParam(value = "staffBadge", required = false) String staffBadge) {
		return success(remoteOrtDormitoryStaffService.status(requireCurrentBadge(), SecurityConstants.FROM_IN));
	}

	/** 外宿状态只能查询当前认证员工，忽略客户端兼容参数中的工号。 */
	private String requireCurrentBadge() {
		String badge = SecurityUtils.getUser().getUsername();
		if (StringUtils.isBlank(badge)) {
			throw new TCEException("当前登录员工信息缺失");
		}
		return badge;
	}

	/**
	 * 获取员工基本信息
	 *
	 * @param employeeId 员工工号
	 * @return
	 */
	@GetMapping("/baseinfo")
	public Result getBaseinfo(@RequestParam(value = "employeeId", required = false) String employeeId) {
		return success(service.getBaseinfo(employeeId));
	}

	/***
	 * 根据员工号获取员工二维码
	 * @return
	 */
	@GetMapping("/qrcode")
	public Result qrcode() {
		return service.qrcode();
	}


	/**
	 * getFullinfo
	 * @param employeeId employeeId
	 * @return
	 */
	@GetMapping("/fullinfo")
	public Result getFullinfo(@RequestParam(value = "employeeId", required = false) String employeeId) {
		return success(service.getFullinfo(employeeId));
	}


	/**
	 * 更新员工紧急联系人
	 *
	 * @param employeeUpdateAo employeeUpdateAo
	 * @return
	 */
	@PostMapping("/relation/update")
	public Result relationUpdate(@RequestBody EmployeeUpdateAo employeeUpdateAo) {

		return service.relationUpdate(employeeUpdateAo);
	}

	/**
	 * 员工申请内宿
	 *
	 * @param roomApplyVo roomApplyVo
	 * @return
	 */
	@PostMapping("/room/apply")
	public Result roomApply(@RequestBody RoomApplyAo roomApplyVo) {
		return service.roomApply(roomApplyVo);
	}

	/**
	 * 获取外宿申请信息
	 *
	 * @param staffBadge staffBadge
	 * @return
	 */
	@GetMapping("/allowance/get")
	public Result getAllowance(@RequestParam(value = "staffBadge", required = false) String staffBadge, @RequestParam(value = "type") Integer type) {
		return service.getAllowance(staffBadge, type);
	}

	/**
	 * 员工外宿申请
	 *
	 * @param outDormitory outDormitory
	 * @return
	 */
	@PostMapping("/out/room/apply")
	public Result outRoomApply(@RequestBody SmtOutDormitoryStaffDTO outDormitory) {
		return service.outRoomApply(outDormitory);
	}

	/**
	 * 员工外宿详情
	 *
	 * @param staffBadge staffBadge
	 * @return
	 */
	@GetMapping("/out/room/detail")
	public Result outRoomDetaol(@RequestParam(value = "staffBadge", required = false) String staffBadge, @RequestParam(value = "type") Integer type) {
		return service.outRoomDetaol(staffBadge, type);
	}

	/**
	 * 获取员工外宿列表
	 * @param params
	 * @param staffBadge
	 * @return
	 */
	@GetMapping("/out/room/list")
	public Result outRoomList(@RequestParam Map<String, Object> params, @RequestParam(value = "staffBadge", required = false) String staffBadge) {
		return service.outRoomList(params,staffBadge);
	}

	/**
	 * 获取员工外宿列表详情
	 * @param id
	 * @return
	 */
	@GetMapping("/out/room/list/detail")
	public Result outRoomListDetail(@RequestParam Integer id) {
		return service.outRoomListDetail(id);
	}

	/**
	 * 外宿申请记录详情
	 *
	 * @param id id
	 * @return
	 */
	@GetMapping("/out/room/apply/detail")
	public Result outRoomApplyDetail(@RequestParam(value = "id") Integer id) {
		return service.outRoomApplyDetail(id);
	}


	/**
	 * 根据id查询员工外宿详情
	 *
	 * @param recordId
	 * @return
	 */
	@GetMapping("/out/room/apply/detail/byId")
	public Result outRoomDetailById(@RequestParam(value = "recordId") String recordId) {
		return service.outRoomDetailById(recordId);
	}


	/**
	 * 查询员工的宿舍信息
	 *
	 * @return
	 */
	@PostMapping("room/detail")
	public Result roomDetail() {
		return success(service.roomDetail());
	}

	/**
	 * 获取紧急联系人字典表
	 *
	 * @return
	 */
	@GetMapping("/relation/list")
	public Result relationList() {
		List<RelationTypeVO> list = service.relationList();
		return success(list);
	}

	/**
	 * 获取新员工须知列表
	 *
	 * @param parkId parkId
	 * @param page   分页对象
	 * @return
	 */
	@GetMapping("/new/note/list/{parkId}")
	public Result getNoteList(@PathVariable Integer parkId, Page page) {
		EmployeeNoteAo employeeNoteAo = new EmployeeNoteAo();
		employeeNoteAo.setParkId(parkId);
		IPage<AppSubject> iPage = appEmployeeNoteService.getPageList(page, employeeNoteAo);
		return success(iPage, NewEmployeeNoteListVo.class);
	}

	/**
	 * 查看新员工须知详情
	 *
	 * @param noteId noteId
	 * @return
	 */
	@GetMapping("/new/note/detail/{noteId}")
	public Result viewNoteDetails(@PathVariable Integer noteId) {
		AppSubject appSubject = appEmployeeNoteService.noteDetail(noteId);
		return success(appSubject, NewEmployeeNoteDetailVo.class);
	}

	/**
	 * 更新员工人脸信息
	 *
	 * @param ocrAo 人脸照片信息
	 * @return Result<Boolean>
	 */
	@PostMapping("/face/update")
	public Result<Boolean> updateFacePhoto(@RequestBody PerfectInfoAo ocrAo) {
		return new Result<Boolean>(service.updateFacePhoto(ocrAo));
	}

	/**
	 * 同步员工照片信息到C6、EHR
	 *
	 * @return
	 */
	@Inner
	@GetMapping("/sync/photo/c6")
	private Result<Boolean> syncPhotoToC6() {
		return new Result<>(service.syncPhotoToYuto());
	}

	/**
	 *
	 * @param badge badge
	 * @return
	 */
	@GetMapping("/getSalayType/{badge}")
	public Result getSalayType(@PathVariable String badge) {
		EmployeeSalayType type = service.getSalayType(badge);
		return new Result<>(type);
	}

	/**
	 * 添加外宿补贴申请取消
	 * @param staffBadge
	 * @param backDate 撤销月份  yyyy-MM-dd
	 * @return
	 */
	@GetMapping("/addCallowance/cancel")
	public Result addCallowanceCancel(@RequestParam(value = "staffBadge", required = false) String staffBadge,@RequestParam(value = "backDate", required = false) String backDate, @RequestParam(value = "type") Integer type) {
		return service.addCallowanceCancel(staffBadge,backDate, type);
	}


	/**
	 * 外宿申请补贴撤销列表
	 * @param staffBadge
	 * @return
	 */
	@GetMapping("/callowance/cancel/list")
	public Result callowanceCancelList(@RequestParam(value = "staffBadge", required = false) String staffBadge) {
		return service.callowanceCancelList(staffBadge);
	}

	/**
	 * 外宿申请补贴撤销详情
	 * @param id
	 * @return
	 */
	@GetMapping("/callowance/cancel/detail")
	public Result callowanceCancelDetail(@RequestParam(value = "id", required = false) Integer id) {
		return service.callowanceCancelDetail(id);
	}

	/**
	 * 员工及外宿补贴信息
	 * @param staffBadge
	 * @return
	 */
	@GetMapping("/callowance/info")
	public Result callowanceUInfo(@RequestParam(value = "staffBadge", required = false) String staffBadge, @RequestParam(value = "type") Integer type) {
		return service.callowanceUInfo(staffBadge, type);
	}


	/**
	 * 外宿补贴详情
	 * @param staffBadge
	 * @return
	 */
	@GetMapping("/callowance/detail")
	public Result callowanceDetail(@RequestParam(value = "staffBadge", required = false) String staffBadge, @RequestParam(value = "type") Integer type) {
		return service.callowanceDetail(staffBadge, type);
	}


	/**
	 * 获取空闲床位数
	 * @param staffBadge
	 * @return
	 */
	@GetMapping("/freeBed")
	public Result getFreeBed(@RequestParam(value = "parkId") Integer parkId, @RequestParam(value = "staffBadge", required = false) String staffBadge) {
		return service.getFreeBed(parkId, staffBadge);
	}

	/**
	 * 获取外宿补贴开始时间的设置
	 * @return
	 */
	@GetMapping("/out/room/getDormitorySet")
	public Result<Integer> getDormitorySet() {
		return service.getDormitorySet();
	}
}
