package com.tce.smart.app.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.dto.AppPictureDto;
import com.tce.smart.app.dto.AppQuestionDto;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppContentPictureService;
import com.tce.smart.app.service.AppQuestionService;
import com.tce.smart.app.service.AppSubjectService;
import com.tce.smart.app.vo.AppQuestionVo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/appquestion")
public class AppQuestionController extends BaseController {
	@Autowired
	private AppQuestionService appQuestionService;
	@Autowired
	private AppSubjectService appSubjectService;
	/**
	 * 新增问题答案类容
	 * @param appQuestionDto 文本内容
	 * @return Result
	 */
	@SysLog("新增问题内容")
	@PostMapping("/addQuestion")
	public Result saveAnswer(@RequestBody AppQuestionDto appQuestionDto){
		Integer id = appQuestionService.addQuestion(appQuestionDto);
		return success(id);
	}

	/**
	 * 修改问题答案内容
	 * @param appQuestionDto
	 * @return
	 */
	@SysLog("更新问题内容")
	@PostMapping("/updateQuestion")
	public Result updateQuestion(@RequestBody AppQuestionDto appQuestionDto){
		appQuestionService.updateQuestion(appQuestionDto);
		return success();
	}

	/**
	 * 分页查询问题信息
	 * @param page 分页对象
	 * @param appQuestionDto 传入条件，问题类（问题名字和时间）
	 * @return
	 */
	@GetMapping("/questionPage")
	public Result getAppQuestionPage(Page page, AppQuestionDto appQuestionDto) {
		IPage<AppSubject> iPage = appSubjectService.getAppQuestionPage(page,appQuestionDto);
		return  success(iPage, AppQuestionVo.class);
	}

	/**
	 * 通过id删除问题信息
	 * @param id id
	 * @return Result
	 */
	@GetMapping("/deleteQs/{id}")
	public Result deleteQuestionById(@PathVariable("id") Integer id){
		appSubjectService.deleteQuestion(id);
		return success();
	}

	/**
	 * 通过id查询问题信息
	 * @param id id
	 * @return Result
	 */
	@GetMapping("/detailQs/{id}")
	public Result detailQuestionById(@PathVariable("id") Integer id){
		AppSubject appSubject = appSubjectService.detailQuestionById(id);
		return success(appSubject,AppQuestionVo.class);
	}

}
