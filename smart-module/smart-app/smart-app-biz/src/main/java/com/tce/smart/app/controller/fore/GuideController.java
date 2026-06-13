package com.tce.smart.app.controller.fore;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.dto.AppQuestionDto;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppSubjectService;
import com.tce.smart.app.service.fore.GuideService;
import com.tce.smart.app.vo.fore.QuestionDetailVo;
import com.tce.smart.app.vo.fore.QuestionListVo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

/**
 * 帮助引导Controller
 *
 * @author mingkai.wu
 * @date 2019-05-09 15:15:12
 */
@RestController
@RequestMapping("/guide")
public class GuideController extends BaseController{

	@Autowired
	private GuideService guideService;

	@Autowired
	private AppSubjectService appSubjectService;

	/**
	 * 获取欢迎页内容
	 *
	 * @param params 分页参数
	 * @return Result<?>
	 */
	@GetMapping("/welcome")
	public Result<?> getParkList(@RequestParam Map<String, Object> params) {
		return new Result<>(guideService.getWelcome(params));
	}

	/**
	 * 获取常见问题列表
	 *
	 * @param params 分页参数
	 * @return Result<?>
	 */
	@GetMapping("/help/question/list")
	public Result<?> getQuestionList(Page page) {
		IPage<AppSubject> iPage = appSubjectService.getAppQuestionPage(page,new AppQuestionDto());
		return  success(iPage, QuestionListVo.class);
	}

	/**
	 * 获取常见问题解答
	 *
	 * @param params 分页参数
	 * @return Result<?>
	 */
	@GetMapping("/help/question/answer/{questionId}")
	public Result<?> getQADetail(@PathVariable Integer questionId) {
		AppSubject appSubject = appSubjectService.detailQuestionById(questionId);
		return  success(appSubject,QuestionDetailVo.class);
	}
}
