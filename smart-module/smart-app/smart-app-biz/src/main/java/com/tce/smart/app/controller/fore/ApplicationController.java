package com.tce.smart.app.controller.fore;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tce.smart.app.ao.fore.ApplicationAo;
import com.tce.smart.app.ao.fore.OperationAo;
import com.tce.smart.app.service.fore.ApplicationService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

import lombok.AllArgsConstructor;

/**
 * 招聘管理控制器
 * @author qipei
 *
 */
@RestController
@AllArgsConstructor
@RequestMapping("/job/application")
public class ApplicationController extends BaseController {

	private ApplicationService applicationService;

	/**
	 * 获取简历列表
	 * @param params
	 * @param application
	 * @return
	 */
	@PostMapping("/list")
	public Result getApplicationList(@RequestParam Map<String, Object> params, @RequestBody ApplicationAo application)
	{
		return applicationService.getApplicationList(params,application);
	}

	/**
	 * 查看简历详情
	 * @param applicationId 应聘记录id
	 * @return
	 */
	@PostMapping("/detail")
	public Result getApplicationDetail(@RequestBody String applicationId)
	{
		return success(applicationService.getApplicationDetail(applicationId));
	}

	/**
	 * 获取所有发布的招聘的岗位
	 * @return
	 */
	@GetMapping("/jobsift/list/{parkId}")
	public Result getJobsiftList(@PathVariable Integer parkId)
	{
		return  applicationService.getJobsiftList(parkId);
	}

	/**
	 * 获取简历状态类型列表
	 * @return
	 */
	@GetMapping("/otptype/list")
	public Result getOtptypeList()
	{
		return  applicationService.getOtptypeList();
	}

	/**
	 * 筛选简历
	 * @param
	 * @return
	 */
	@PostMapping("/operation")
	public Result operationApplication(@RequestBody OperationAo operationAo)
	{
		return applicationService.operationApplication(operationAo);
	}

	/**
	 * 查看应聘记录
	 * @param applicationId 应聘记录id
	 * @return
	 */
	@PostMapping("/record")
	public Result getRecord(@RequestBody String applicationId)
	{

		return applicationService.getRecord(applicationId);
	}

	/**
	 * 人脸搜索简历
	 * @param facePhoto 人脸招聘base64
	 * @return
	 */
	@PostMapping("/face/list")
	public Result getFaceList(@RequestBody String facePhoto)
	{
		return applicationService.getFaceList(facePhoto);
	}


}
