package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.api.feign.RemoteAppModuleService;
import com.tce.smart.app.api.vo.AppModuleSimpleInfoVo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.data.api.vo.msg.SmtAppAuthDetailVo;
import com.tce.smart.data.api.vo.msg.SmtAppAuthListVo;
import com.tce.smart.data.api.vo.msg.SmtAppHrAuthListVo;
import com.tce.smart.platform.core.ao.SmtAppAuthSaveAO;
import com.tce.smart.platform.core.entity.SmtAppAuth;
import com.tce.smart.platform.service.SmtAppAuthService;
import com.tce.smart.platform.service.SmtAppHrAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * App权限控制器
 *
 * @author mckaywu
 * @date 2019-06-12 11:25:48
 */
@RestController
@RequestMapping("/appauth")
public class SmtAppAuthController extends BaseController {

	@Autowired
	private SmtAppAuthService smtAppAuthService;

	@Autowired
	private SmtAppHrAuthService smtAppHrAuthService;

	@Autowired
	private RemoteAppModuleService remoteAppModuleService;

	/**
	 * 分页查询权限列表
	 *
	 * @param page       分页对象
	 * @param smtAppAuth
	 * @return
	 */
	@GetMapping("/page")
	public Result getAuthPage(Page<SmtAppAuth> page, SmtAppAuth smtAppAuth) {
		IPage<SmtAppAuth> iPage = smtAppAuthService.getSmtAuthPage(page, smtAppAuth);
		return success(iPage, SmtAppAuthListVo.class);
	}

	/**
	 * 查询所有权限列表
	 *
	 * @return
	 */
	@GetMapping("/list")
	public Result getAuthList() {
		List<SmtAppAuth> list = smtAppAuthService.getAuthList();
		return success(list, SmtAppAuthListVo.class);
	}

	/**
	 * 查询HR招聘数据权限列表
	 *
	 * @return
	 */
	@GetMapping("/hr/auth/list")
	public Result getHrAuthList() {
		return success(smtAppHrAuthService.getHrAuthList(), SmtAppHrAuthListVo.class);
	}

	/**
	 * 通过id查询权限
	 *
	 * @param id 权限ID
	 * @return Result
	 */
	@GetMapping("/{id}")
	public Result getById(@PathVariable("id") Integer id) {
		return success(smtAppAuthService.getById(id), SmtAppAuthDetailVo.class);
	}

	/**
	 * 根据园区ID查询是否存在初始化权限
	 * @param parkId
	 * @return
	 */
	@GetMapping("/init/flag/{parkId}")
	public Result<Boolean> getInitFlag(@PathVariable("parkId") Integer parkId) {
		return success(smtAppAuthService.getInitFlag(parkId));
	}

	/**
	 * 新增权限
	 *
	 * @param appAuthSaveAO app权限新增Ao
	 * @return Result
	 */
	@SysLog("新增")
	@PostMapping("/save")
	public Result save(@RequestBody @Valid SmtAppAuthSaveAO appAuthSaveAO) {
		return success(smtAppAuthService.addAuth(appAuthSaveAO));
	}

	/**
	 * 修改权限
	 *
	 * @param appAuthSaveAO app权限修改Ao
	 *
	 * @return Result
	 */
	@SysLog("修改")
	@PostMapping("/update")
	public Result updateById(@RequestBody @Valid SmtAppAuthSaveAO appAuthSaveAO) {
		return success(smtAppAuthService.updateAuthById(appAuthSaveAO));
	}

	/**
	 * 通过id删除权限
	 *
	 * @param id 权限ID
	 * @return Result
	 */
	@SysLog("删除")
	@PostMapping("/delete/{id}")
	public Result removeById(@PathVariable Integer id) {
		return success(smtAppAuthService.removeAuthById(id));
	}

	/**
	 * 获取App业务模块
	 * @return
	 */
	@GetMapping("/module/business/simple")
	public Result<List<AppModuleSimpleInfoVo>> getSimpleBusModule(){
		Result<List<AppModuleSimpleInfoVo>> result = remoteAppModuleService.getSimpleBusModule();
		return result ;
	}

}
