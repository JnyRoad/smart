package com.tce.smart.app.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.ao.AddAppSubjectAo;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppBannerService;
import com.tce.smart.app.service.AppSubjectService;
import com.tce.smart.app.vo.AppBannerVo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author fushiping
 * @date 2019/5/22 14:30
 **/
@RestController
@AllArgsConstructor
@RequestMapping("/appbanner")
public class AppBannerController extends BaseController {

	private final AppBannerService appBannerService;

	private final AppSubjectService appSubjectService;

	/**
	 * 分页查询已上线主题内容
	 * @param page 分页对象
	 * @return
	 */
	@GetMapping("/page/list")
	public Result appBannerList(Page page) {
		IPage<AppSubject> iPage = appBannerService.getBannerList(page);
		return success(iPage, AppBannerVo.class);
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
	 * 上线
	 * @param id
	 * @return
	 */
	@GetMapping("/onlineSubject/{id}")
	public Result onlineById(@PathVariable Integer id) {
		appBannerService.onlineById(id);
		return success();
	}

	/**
	 * 取消上线
	 * @param id
	 * @return
	 */
	@GetMapping("/offlineSubject/{id}")
	public Result offlineFlagById(@PathVariable Integer id) {
		appBannerService.waitById(id);
		return success();
	}

	/**
	 * 根据id删除主题
	 * @param id
	 * @return
	 */
	@GetMapping("/delete/{id}")
	public Result deleteById(@PathVariable Integer id) {
		appBannerService.deleteById(id);
		return success();
	}

	/**
	 * 新增主题
	 * @param addAppSubjectAo
	 * @return
	 */
	@PostMapping("/addSubject")
	public Result addSubject(@RequestBody AddAppSubjectAo addAppSubjectAo) {
		Integer subjectId = appBannerService.addSubject(addAppSubjectAo);
		return success(subjectId);
	}
	/**
	 * 修改主题
	 * @param addAppSubjectAo
	 * @return
	 */
	@PostMapping("/updateSubject")
	public Result updateSubject(@RequestBody AddAppSubjectAo addAppSubjectAo) {
		appBannerService.updateSubject(addAppSubjectAo);
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
		return success(appSubject, AppBannerVo.class);
	}

}
