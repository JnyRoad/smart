package com.tce.smart.app.controller;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.ao.AddAppSubjectAo;
import com.tce.smart.app.ao.AppealAreaAo;
import com.tce.smart.app.ao.EmployeeNoteAo;
import com.tce.smart.app.ao.fore.AppSubjectDetailAO;
import com.tce.smart.app.emun.PublishState;
import com.tce.smart.app.emun.SubjectCatalog;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppContentTextService;
import com.tce.smart.app.service.AppEmployeeNoteService;
import com.tce.smart.app.service.AppSubjectService;
import com.tce.smart.app.service.AppealAreaService;
import com.tce.smart.app.vo.AppEmployeeNoteListVo;
import com.tce.smart.app.vo.AppEmployeeNoteVo;
import com.tce.smart.app.vo.AppSubjectListVo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;

/**
 * @description: 申诉专区控制器
 * @date: 2020-07-28 11:20
 * @author: wuling
 * @version: 1.0
 */
@RestController
@AllArgsConstructor
@Api(tags = "申诉专区")
@RequestMapping("/appealarea")
public class AppealAreaController extends BaseController {

	private final AppealAreaService appealAreaService;

	private final AppSubjectService appSubjectService;

	/**
	 *  分页查询申诉专区文章列表
	 * @param page
	 * @param appealAreaAo
	 * @return
	 */
	@ApiIgnore
	@PostMapping("/page/list")
	public Result getPageList(Page page, @RequestBody(required = false) AppealAreaAo appealAreaAo) {
		if(appealAreaAo == null){
			appealAreaAo = new AppealAreaAo();
		}
		appealAreaAo.setPublishFlag(PublishState.ONLINE.getCode());
		appealAreaAo.setCatalogCode(SubjectCatalog.ARREAL_AREA.type());
		IPage<AppSubject> iPage = appealAreaService.getAppSubjectPage(page,appealAreaAo);
		return success(iPage, AppSubjectListVo.class);
	}

	/**
	 *  添加申诉专区文章
	 * @param addAppSubjectAo
	 * @return
	 */
	@ApiIgnore
	@PostMapping("/add")
	public Result noteAdd(@RequestBody AddAppSubjectAo addAppSubjectAo) {
		return success(appealAreaService.addAppealAreaArticle(addAppSubjectAo));
	}

	/**
	 *  申诉文章详情
	 * @param id
	 * @return
	 */
	@ApiIgnore
	@GetMapping("/detail/{id}")
	public Result noteDetail(@PathVariable Integer id) {
		Assert.notNull(id,"记录Id不能为空");
		AppSubject appSubject = appealAreaService.getAppealArticleDetail(id);
		return success(appSubject, AppEmployeeNoteVo.class);
	}

	/**
	 *  修改申诉专区文章
	 * @param addAppSubjectAo
	 * @return
	 */
	@ApiIgnore
	@PostMapping("/update")
	public Result noteUpdate(@RequestBody AddAppSubjectAo addAppSubjectAo) {
		return success(appealAreaService.updateAppealArticle(addAppSubjectAo));
	}

	/**
	 *  删除申诉文章记录
	 * @param id
	 * @return
	 */
	@ApiIgnore
	@PostMapping("/del/{id}")
	public Result noteDel(@PathVariable Integer id) {
		return success(appealAreaService.delAppealArticleRecord(id));
	}

	/**
	 *  app分页查询申诉专区文章列表
	 * @param current 当前页
	 * @param size 大小
	 * @param parkId 园区ID
	 * @return
	 */
	@ApiOperation("APP查询申诉专区文章")
	@GetMapping("/app/list")
	public Result<IPage<AppSubjectListVo>> getAppPageList(@ApiParam(name = "current",value = "当前页",required = true) @RequestParam long current,
								 @ApiParam(name = "size",value = "大小",required = true) @RequestParam long size,
								 @ApiParam(name = "parkId",value = "园区Id",required = true) @RequestParam Integer parkId) {
		AppealAreaAo appealAreaAo = new AppealAreaAo();
		appealAreaAo.setParkId(parkId);
		appealAreaAo.setPublishFlag(PublishState.ONLINE.getCode());
		appealAreaAo.setCatalogCode(SubjectCatalog.ARREAL_AREA.type());
		IPage<AppSubject> iPage = appealAreaService.getAppSubjectPage(new Page(current,size),appealAreaAo);
		return success(iPage, AppSubjectListVo.class);
	}

	/**
	 *  app端获取申诉文章详情
	 * @param id
	 * @return
	 */
	@ApiOperation("APP查询申诉专区文章详情")
	@GetMapping("/app/detail/{id}")
	public Result<AppSubjectDetailAO> noteDetailByApp(@ApiParam(name = "id",value = "文章标识Id",required = true) @PathVariable Integer id) throws IOException {
		return success(appealAreaService.noteDetailByApp(id));
	}
}
