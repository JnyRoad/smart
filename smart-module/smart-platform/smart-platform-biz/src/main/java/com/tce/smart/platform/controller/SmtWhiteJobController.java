package com.tce.smart.platform.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.dto.WhiteJobRemoveDTO;
import com.tce.smart.platform.core.entity.SmtWhiteJob;
import com.tce.smart.platform.service.SmtWhiteJobService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 无需二级审批预约的岗位
 * @author QIPEI
 *
 */
@RestController
@AllArgsConstructor
@RequestMapping("/white/job")
public class SmtWhiteJobController  extends BaseController {


	@Autowired
	private SmtWhiteJobService smtWhiteJobService;

	/**
	 * 分页查询无需二级审批预约的岗位
	 * @param page
	 * @param smtWhiteJob
	 * @return
	 */
	@GetMapping("/page")
	public Result getWhiteJobPage(Page page, SmtWhiteJob smtWhiteJob) {
		if(ObjectUtil.isNull(smtWhiteJob.getJobName()))
		{
			smtWhiteJob.setJobName("");
		}
		return new Result<>(smtWhiteJobService.page(page,smtWhiteJob));

	}


	/**
	 * 移除无需二级审批预约的岗位
	 * @param whiteJobRemoveDTO
	 * @return
	 */
	@SysLog("删除黑名单访客")
	@PostMapping("/delete")
	public Result deleteById(@RequestBody WhiteJobRemoveDTO  whiteJobRemoveDTO) {
		return smtWhiteJobService.removeVisitorById(whiteJobRemoveDTO);
	}


	@SysLog("新增无需二级审批预约的岗位")
	@PostMapping("/add")
	public Result save(@RequestBody SmtWhiteJob smtWhiteJob) {
		return smtWhiteJobService.saveWhiteJob(smtWhiteJob);
	}
}
