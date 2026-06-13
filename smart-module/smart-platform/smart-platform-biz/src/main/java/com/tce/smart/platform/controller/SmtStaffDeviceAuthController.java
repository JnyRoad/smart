package com.tce.smart.platform.controller;


		import com.tce.smart.platform.api.dto.resp.DeviceTaskInfoRespDTO;
		import com.tce.smart.tool.enums.BusinessAuthorityEnum;
		import org.springframework.beans.factory.annotation.Autowired;
		import org.springframework.web.bind.annotation.*;

		import com.tce.smart.common.core.model.Result;
		import com.tce.smart.common.core.wrapper.BaseController;
		import com.tce.smart.common.log.annotation.SysLog;
		import com.tce.smart.platform.core.dto.UpdateDeviceAuthDTO;
		import com.tce.smart.platform.core.entity.SmtStaffDeviceAuth;
		import com.tce.smart.platform.service.SmtStaffDeviceAuthService;

/**
 * 员工通关权限控制器
 *
 * @author qipei
 * @date 2019-06-15 21:39:20
 */
@RestController
@RequestMapping("/staff/device/auth")
public class SmtStaffDeviceAuthController extends BaseController {

	@Autowired
	private SmtStaffDeviceAuthService smtStaffDeviceAuthService;


	/**
	 * 通过id删除权限
	 *
	 * @param id 权限ID
	 * @return Result
	 */
	@SysLog("删除")
	@GetMapping("/delete/{id}")
	public Result removeById(@PathVariable Integer id) {
		return success(smtStaffDeviceAuthService.removeAuthById(id));
	}
//
//	@SysLog("新增员工权限")
//	@PostMapping("addAuth")
//	public Result addAuth(@RequestBody SmtStaffDeviceAuth auth) {
//		auth.setAuthId(BusinessAuthorityEnum.STAFF_FACE.getCode());
//		return success(smtStaffDeviceAuthService.addAuth(auth));
//	}


	/**
	 * 批量修改员工权限策略
	 * type=1: 追加权限
	 * type=2: 覆盖权限
	 */
	@SysLog("修改员工权限")
	@PostMapping("/updateAuth/{type}")
	public Result updateAuth(@PathVariable("type") Integer type, @RequestBody UpdateDeviceAuthDTO auth) {
		return success(smtStaffDeviceAuthService.updateAuthNew(type, auth));
	}

	/**
	 * 权限下发进度
	 */
	@SysLog("权限下发进度")
	@GetMapping("/authInfo")
	public Result updateAuth(@RequestParam("taskRecordStr") String taskRecordStr) {
		return success(smtStaffDeviceAuthService.getTaskResult(taskRecordStr), DeviceTaskInfoRespDTO.class);
	}

}
