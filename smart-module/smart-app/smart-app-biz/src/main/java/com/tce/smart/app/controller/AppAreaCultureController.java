package com.tce.smart.app.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tce.smart.app.ao.AddAppSubjectAo;
import com.tce.smart.app.emun.PublishState;
import com.tce.smart.app.emun.SubjectCatalog;
import com.tce.smart.app.vo.AppSubjectListVo;
import com.tce.smart.app.vo.AppSubjectVo;
import com.tce.smart.common.core.wrapper.BaseController;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppSubjectService;
import lombok.AllArgsConstructor;




/**
 * 企业文化主题板块
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:44:43
 */
@RestController
@AllArgsConstructor
@RequestMapping("/appareaculture")
public class AppAreaCultureController extends BaseController {

	private final  AppSubjectService appSubjectService;

	/**
	 * 分页查询已上线主题内容
	 * @param page 分页对象
	 * @return
	 */
	@RequestMapping(value = {"/pageOnline","/pageOnline/{parkId}"}, method = RequestMethod.GET)
	public Result getAppSubjectOnline(Page page, @PathVariable(required = false) Integer parkId) {
		String publishFlag = PublishState.ONLINE.getCode();
		String catalogCode = SubjectCatalog.PARK_CULTURE.type();
		IPage<AppSubject> iPage = appSubjectService.getAppSubjectPageFilterByPark(page, publishFlag, catalogCode, parkId);
		return success(iPage, AppSubjectListVo.class);
	}

	/**
	 * 分页查询已下线主题内容
	 * @param page 分页对象
	 * @return
	 */
	@RequestMapping(value = {"/pageOffline","/pageOffline/{parkId}"}, method = RequestMethod.GET)
	public Result getAppSubjectOffline(Page page, @PathVariable(required = false) Integer parkId) {
		String publishFlag = PublishState.OFFLINE.getCode();
		String catalogCode = SubjectCatalog.PARK_CULTURE.type();
		IPage<AppSubject> iPage = appSubjectService.getAppSubjectPageFilterByPark(page, publishFlag, catalogCode, parkId);
		return success(iPage, AppSubjectListVo.class);
	}

	/**
	 * 分页查询待发布主题内容
	 * @param page 分页对象
	 * @return
	 */
	@RequestMapping(value = {"/pageWait","/pageWait/{parkId}"}, method = RequestMethod.GET)
	public Result getAppSubjectWait(Page page, @PathVariable(required = false) Integer parkId) {
		String publishFlag = PublishState.INIT.getCode();
		String catalogCode = SubjectCatalog.PARK_CULTURE.type();
		IPage<AppSubject> iPage = appSubjectService.getAppSubjectPageFilterByPark(page, publishFlag, catalogCode, parkId);
		return success(iPage, AppSubjectListVo.class);
	}

	/**
	 * 主题上移
	 * @param id
	 * @return
	 */
	@GetMapping("/moveUpward/{id}")
	public Result appSubjectMoveUpward(@PathVariable Integer id) {
		appSubjectService.moveUpwardById(id);
		return success();
	}

	/**
	 * 主题下移
	 * @param id
	 * @return
	 */
	@GetMapping("/moveDown/{id}")
	public Result appSubjectMoveDown(@PathVariable Integer id) {
		appSubjectService.moveDownById(id);
		return success();
	}

	/**
	 * 主题置顶
	 * @param id
	 * @return
	 */
	@GetMapping("/letTop/{id}")
	public Result appSubjectLetTop(@PathVariable Integer id) {
		appSubjectService.letTopById(id);
		return success();
	}

	/**
	 * 取消主题置顶
	 * @param id
	 * @return
	 */
	@GetMapping("/cancleTop/{id}")
	public Result appSubjectCancleTopById(@PathVariable Integer id) {
		appSubjectService.cancleTopById(id);
		return success();
	}

	/**
	 * 批量上线
	 * @param ids
	 * @return
	 */
	@PostMapping("/onlineSubject")
	public Result onlineById(@RequestBody int[] ids) {
		appSubjectService.batchOnline(ids);
		return success();
	}

	/**
	 * 批量待发布
	 * @param ids
	 * @return
	 */
	@PostMapping("/waitSubject")
	public Result batchWaitById(@RequestBody int[] ids) {
		appSubjectService.batchWait(ids);
		return success();
	}

	/**
	 * 下线
	 * @param id
	 * @return
	 */
	@GetMapping("/offlineSubject/{id}")
	public Result offlineFlagById(@PathVariable Integer id) {
		appSubjectService.offlineById(id);
		return success();
	}

	/**
	 * 根据id删除主题
	 * @param id
	 * @return
	 */
	@GetMapping("/delete/{id}")
	public Result deleteById(@PathVariable Integer id) {
		appSubjectService.deleteById(id);
		return success();
	}

	/**
	 * 新增主题
	 * @param addAppSubjectAo
	 * @return
	 */
	@PostMapping("/addSubject")
	public Result addSubject(@RequestBody AddAppSubjectAo addAppSubjectAo) {
		Integer subjectId = appSubjectService.subjectInsert(addAppSubjectAo, SubjectCatalog.PARK_CULTURE.type());
		return success(subjectId);
	}

	/**
	 * 修改主题
	 * @param addAppSubjectAo
	 * @return
	 */
	@PostMapping("/updateSubject")
	public Result updateSubject(@RequestBody AddAppSubjectAo addAppSubjectAo) {
		appSubjectService.subjectUpdate(addAppSubjectAo);
		return success();
	}

	/**
	 * 显示主题细节（显示文本内容）
	 * @param id
	 * @return
	 */
	@GetMapping("/subjectDetails/{id}")
	public Result selectSubjectDetails(@PathVariable Integer id) {
		AppSubject appSubject = appSubjectService.subjectDetails(id);
		return success(appSubject, AppSubjectVo.class);
	}

	/**
	 * 根据id批量删除主题
	 * @return
	 */
	@PostMapping("/batchDelete")
	public Result batchDelete(@RequestBody int[] ids) {
		appSubjectService.batchDelete(ids);
		return success();
	}
}
