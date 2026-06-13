package com.tce.smart.platform.controller;


import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.resp.SearchPaperListRespDTO;
import com.tce.smart.platform.core.dto.AddOrUpdatePaperDTO;
import com.tce.smart.platform.core.entity.SmtPaper;
import com.tce.smart.platform.core.entity.SmtParkBu;
import com.tce.smart.platform.core.vo.SearchPaperPageVO;
import com.tce.smart.platform.service.SmtPaperService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/paper")
@Api(tags = "问卷调查-问卷控制类")
public class SmtPaperController extends BaseController {


	private final SmtPaperService smtPaperService;


	/**
	   * 分页查询
	   * @param page 分页对象
	   * @param SmtPaper 问卷表
	   * @return
	   */
	  @GetMapping("/page")
	  @ApiOperation("分页查询问卷列表")
	  public Result<IPage<SearchPaperPageVO>> getSmtParkBuPage(Page page, SmtPaper smtPaper) {
	    return  success(smtPaperService.page(page,smtPaper),SearchPaperPageVO.class);
	  }


	  @ApiOperation("调查问卷详情")
	  @GetMapping("/detail/{id}")
	  public Result detailById(@PathVariable("id") Integer id) {
			return success(smtPaperService.detailById(id));
	  }

	  @ApiOperation("新增问卷")
	  @PostMapping("/add")
	  public Result save(@RequestBody AddOrUpdatePaperDTO addOrUpdatePaperDTO) {
			return success(smtPaperService.addPaper(addOrUpdatePaperDTO));
	  }


	  @ApiOperation("修改问卷")
	  @PostMapping("/update")
	  public Result update(@RequestBody AddOrUpdatePaperDTO addOrUpdatePaperDTO) {
			return success(smtPaperService.update(addOrUpdatePaperDTO));
	  }


	  @ApiOperation("删除问卷")
	  @GetMapping("/delete/{id}")
	  public Result delete(@PathVariable("id") Integer id) {
			return success(smtPaperService.remove(id));
	  }



	  @ApiOperation("获取园区的bu")
	  @GetMapping("/getBu/{parkId}")
	  public Result<List<SmtParkBu>> getBu(@PathVariable("parkId") Integer parkId) {
			return success(smtPaperService.getBu(parkId));
	  }





	  @ApiOperation("更新问卷的状态")
	  @GetMapping("/status/refresh")
	  public void statusRefresh() {
			smtPaperService.statusRefresh();
	  }


	  @ApiOperation("app接口--获取员工可以查看的文件")
	  @GetMapping("/getPaperByBadge/{badge}")
	  public Result<List<SearchPaperListRespDTO>> getPaperByBadge(@PathVariable("badge") String badge) {
			return success(smtPaperService.getPaperByBadge(badge));
	  }


}
