package com.tce.smart.app.controller.fore;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.emun.PublishState;
import com.tce.smart.app.emun.SubjectCatalog;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppBannerService;
import com.tce.smart.app.service.AppSubjectService;
import com.tce.smart.app.service.fore.HomePagetService;
import com.tce.smart.app.vo.fore.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * App首页控制器
 *
 * @author fushiping
 *
 */
@RestController
@RequestMapping("/home")
public class HomepageController extends BaseController {

	@Autowired
	private AppSubjectService appSubjectService;

	@Autowired
	private AppBannerService appBannerService;

	@Autowired
	private HomePagetService homePagetService;

	/**
	 * 获取园区已上线Banner列表
	 *
	 * @param
	 * @return
	 */
	@GetMapping("/banner")
	public Result appBanner() {
		List<AppSubject> list = appBannerService.getOnlineBannerList();
		return success(list, HomeBannerVo.class);
	}

	/**
	 * 获取园区导航菜单列表
	 *
	 * @param parkId
	 * @return
	 */
	@GetMapping("/menu")
	public Result<?> appMenu(@RequestParam("parkId") String parkId) {
		return success(homePagetService.getNavigateModule(parkId));
	}

	/**
	 * 获取集团公告已上线列表
	 *
	 * @param page 分页对象
	 * @return
	 */
	@RequestMapping(value = "/bbs/list", method = RequestMethod.GET)
	public Result appNoticeOnline(Page page) {
		IPage<AppSubject> iPage = appSubjectService.getAppSubjectPage(page, PublishState.ONLINE.getCode(),
				SubjectCatalog.PARK_NOTICE.type(),null);
		return success(iPage, BbsListVo.class);
	}

	/**
	 * 查看集团公告详情
	 *
	 * @param bbsId
	 * @return
	 */
	@GetMapping("/bbs/detail/{bbsId}")
	public Result appNoticeDetails(@PathVariable String bbsId) {
		AppSubject appSubject = appSubjectService.subjectDetails(Integer.valueOf(bbsId));
		return success(appSubject, BbsDetailVo.class);
	}

	/**
	 * 获取园区新闻已上线列表
	 *
	 * @param page 分页对象
	 * @return
	 */
	@GetMapping("/news/list")
	public Result<?> appNewsOnline(Page<?> page, @RequestParam(required = true)Integer parkId) {
		IPage<AppSubject> iPage = appSubjectService.getAppSubjectPage(page, PublishState.ONLINE.getCode(),
				SubjectCatalog.PARK_NEWS.type(), parkId);
		return success(iPage, NewsListVo.class);
	}

	/**
	 * 查看园区新闻详情
	 *
	 * @param newsId
	 * @return
	 */
	@GetMapping("/news/detail/{newsId}")
	public Result<?> appNewsDetails(@PathVariable String newsId) {
		AppSubject appSubject = appSubjectService.subjectDetails(Integer.valueOf(newsId));
		return success(appSubject, NewsDetailVo.class);
	}

	/**
	 * 获取园区概况列表
	 *
	 * @param page 分页对象
	 * @return
	 */
	@GetMapping("/park/general/list")
	public Result<?> getParkGeneral(Page<?> page, @RequestParam(required = true)Integer parkId) {
		IPage<AppSubject> iPage = appSubjectService.getAppSubjectPage(page, PublishState.ONLINE.getCode(),
				SubjectCatalog.PARK_GENERAL.type(), parkId);
		return success(iPage, ParkGeneralListVo.class);
	}

	/**
	 * 查看园区概况详情
	 *
	 * @param generalId 概况ID
	 * @return
	 */
	@GetMapping("/park/general/detail/{generalId}")
	public Result<?> getParkGeneralDetails(@PathVariable String generalId) {
		AppSubject appSubject = appSubjectService.subjectDetails(Integer.valueOf(generalId));
		return success(appSubject, ParkGeneralDetailVo.class);
	}

	/**
	 * 获取园区公告列表
	 *
	 * @param page 分页对象
	 * @return
	 */
	@GetMapping("/park/activity/list")
	public Result<?> getParkActivity(Page<?> page, @RequestParam(required = true)Integer parkId) {
		IPage<AppSubject> iPage = appSubjectService.getAppSubjectPage(page, PublishState.ONLINE.getCode(),
				SubjectCatalog.PARK_ACTIVITY.type(), parkId);
		return success(iPage, ParkActivityListVo.class);
	}

	/**
	 * 查看园区公告详情
	 *
	 * @param activityId 活动ID
	 * @return
	 */
	@GetMapping("/park/activity/detail/{activityId}")
	public Result<?> getParkActivityDetails(@PathVariable String activityId) {
		AppSubject appSubject = appSubjectService.subjectDetails(Integer.valueOf(activityId));
		return success(appSubject, ParkActivityDetailVo.class);
	}

	/**
	 * 获取裕同简介列表
	 *
	 * @param page 分页对象
	 * @return
	 */
	@GetMapping("/park/instroduce/list")
	public Result<?> getParkInstroduce(Page<?> page, @RequestParam(required = true)Integer parkId) {
		IPage<AppSubject> iPage = appSubjectService.getAppSubjectPage(page, PublishState.ONLINE.getCode(),
				SubjectCatalog.PARK_INTRODUCE.type(), parkId);
		return success(iPage, ParkInstroduceListVo.class);
	}

	/**
	 * 查看裕同简介详情
	 *
	 * @param instroduceId 简介ID
	 * @return
	 */
	@GetMapping("/park/instroduce/detail/{instroduceId}")
	public Result<?> getParkiInstroduceDetails(@PathVariable String instroduceId) {
		AppSubject appSubject = appSubjectService.subjectDetails(Integer.valueOf(instroduceId));
		return success(appSubject, ParkInstroduceDetailVo.class);
	}

	/**
	 * 获取裕同文化列表
	 *
	 * @param page 分页对象
	 * @return
	 */
	@GetMapping("/park/culture/list")
	public Result<?> getParkCulture(Page<?> page, @RequestParam(required = true)Integer parkId) {
		IPage<AppSubject> iPage = appSubjectService.getAppSubjectPage(page, PublishState.ONLINE.getCode(),
				SubjectCatalog.PARK_CULTURE.type(), parkId);
		return success(iPage, ParkCultureListVo.class);
	}

	/**
	 * 查看裕同文化详情
	 *
	 * @param cultureId 文化详情
	 * @return
	 */
	@GetMapping("/park/culture/detail/{cultureId}")
	public Result<?> getParkiCultureDetails(@PathVariable String cultureId) {
		AppSubject appSubject = appSubjectService.subjectDetails(Integer.valueOf(cultureId));
		return success(appSubject, ParkCultureDetailVo.class);
	}
}
