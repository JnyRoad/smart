package com.tce.smart.platform.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.SmtProcessRecord;
import com.tce.smart.platform.service.SmtProcessRecordService;

import lombok.AllArgsConstructor;


/**
 * 流程审批记录表
 *
 * @author 梁圆
 * @date 2019-05-15 11:34:58
 */
@RestController
@AllArgsConstructor
@RequestMapping("/processRecord")
public class SmtProcessRecordController extends BaseController{

	private final  SmtProcessRecordService smtProcessRecordService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param SmtProcessRecord 警报信息记录
	 * @return
	 */
	@GetMapping("/page")
	public Result getSmtProcessRecordPage(Page page, SmtProcessRecord SmtProcessRecord) {
		return  new Result <>(smtProcessRecordService.page(page,Wrappers.query(SmtProcessRecord)));
	}


	/**
	 * 新增警报信息记录
	 * @param SmtProcessRecord 警报信息记录
	 * @return Result
	 */
	@SuppressWarnings("rawtypes")
	@SysLog("新增审批记录信息记录")
	@PostMapping("/add")
	public Result saveProcessRecord(SmtProcessRecord SmtProcessRecord){
		boolean save = smtProcessRecordService.save(SmtProcessRecord);
		if(save) {
			return success();
		}
		return fail("添加失败");
	}

}
