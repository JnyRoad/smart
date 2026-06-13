package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.AddFeedBackReqDTO;
import com.tce.smart.platform.core.dto.FeedBackQueryDTO;
import com.tce.smart.platform.core.entity.SmtFeedBack;
import com.tce.smart.platform.service.SmtFeedBackService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 员工问题反馈控制器
 * @author 齐佩
 *
 */
@RestController
@AllArgsConstructor
@RequestMapping("/feed/back")
public class SmtFeedBackController extends BaseController {

	  private final  SmtFeedBackService smtFeedBackService;

	  /**
	   * 分页查询
	   * @param page 分页对象
	   * @param feedBackQueryDTO 随行人员表
	   * @return
	   */
	  @GetMapping("/page")
	  public Result getSmtFeedBackPage(Page page, FeedBackQueryDTO feedBackQueryDTO) {
	    return  new Result<>(smtFeedBackService.page(page,feedBackQueryDTO));
	  }


	  /**
	   * 根据id查询反馈问题详情
	   * @param id
	   * @return
	   */
	  @GetMapping("/detail/{id}")
	  public Result getDetailById(@PathVariable Integer id) {
	    return  new Result<>(smtFeedBackService.getDetailById(id));
	  }

	  /**
	   * 更新问题反馈
	   * @param smtFeedBack
	   * @return
	   */
	  @PostMapping("/update")
	  public Result updateSmtFeedBack(@RequestBody SmtFeedBack smtFeedBack) {
	    return  new Result<>(smtFeedBackService.updateSmtFeedBack(smtFeedBack));
	  }


	  /**
	   * 添加问题反馈
	   * @param feedBack
	   * @return
	   */
	  @SysLog("app接口-添加问题反馈")
	  @PostMapping("/add")
	  public Result addSmtFeedBack(@RequestBody AddFeedBackReqDTO feedBack) {
	    return  new Result<>(smtFeedBackService.addSmtFeedBack(feedBack));
	  }



}
