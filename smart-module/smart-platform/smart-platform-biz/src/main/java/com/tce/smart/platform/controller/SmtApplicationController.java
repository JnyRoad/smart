package com.tce.smart.platform.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.ApplicationInfoRespDTO;
import com.tce.smart.platform.api.dto.req.SaveWechatApplicationReqDTO;
import com.tce.smart.platform.api.dto.req.UpApplicationListReqDTO;
import com.tce.smart.platform.api.dto.resp.ApplicationListRespDTO;
import com.tce.smart.platform.api.dto.resp.SmtApplicationProcessRespDTO;
import com.tce.smart.platform.api.dto.resp.SmtApplicationRespDTO;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.entity.SmtApplication;
import com.tce.smart.platform.core.entity.SmtApplicationProcess;
import com.tce.smart.platform.core.vo.ApplicationInfoVO;
import com.tce.smart.platform.core.vo.FaceApplicationVO;
import com.tce.smart.platform.service.SmtApplicationService;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 应聘表
 *
 * @author 齐佩
 * @date 2019-04-13 18:18:24
 */
@RestController
@AllArgsConstructor
@RequestMapping("/application")
public class SmtApplicationController extends BaseController {

	private final SmtApplicationService smtApplictionService;

	/**
	 * 分页查询应聘基本信息
	 *
	 * @param page
	 * 分页对象
	 * @return
	 */
	@GetMapping("/page")
	public Result getSmtApplictionPage(Page page, ApplicationDTO applicationDTO,@RequestParam(value = "rangTime",required=false) String rangTime,@RequestParam(value = "ageRang",required=false) String ageRang) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		return new Result<>(smtApplictionService.getSmtApplictionPage(page, applicationDTO,rangTime,ageRang, parkIds));
	}

	/**
	 * 通过id查询应聘表详情
	 *
	 * @param id
	 *            id
	 * @return Result
	 */
	@GetMapping("/{id}")
	public Result getById(@PathVariable("id") String id) {
		return smtApplictionService.getApplictionInfoById(id);
	}

	@SysLog("查询应聘流程")
	@GetMapping("/getProcess/{id}")
	public Result getProcess( @PathVariable("id") String id)
	{
		return smtApplictionService.getProcess(id);
	}

	/**
	 * 新增应聘表
	 *
	 * @return Result
	 */
	@SysLog("新增应聘表")
	@PostMapping("addAppliction")
	public Result save(@RequestBody AddOrUpApplicationDTO application) {
		return smtApplictionService.addAppliction(application);
	}

	/**
	 * 微信公众号添加应聘信息
	 *
	 * @return Result
	 */
	@SysLog("微信公众号添加应聘信息")
	@PostMapping("/wechat/add")
	public Result<Long> addWechatAppliction(@RequestBody SaveWechatApplicationReqDTO saveWechatApplicationReqDTO) {
		//smtApplictionService.addWechatAppliction(saveApplicationDTO);
		SaveWechatApplicationDTO saveApplicationDTO = new SaveWechatApplicationDTO();
		BeanUtils.copyProperties(saveWechatApplicationReqDTO,saveApplicationDTO);
		return smtApplictionService.addWechatAppliction(saveApplicationDTO);
	}

	/**
	 * 微信关照添加应聘者手机号
	 *
	 * @return Result
	 */
	@SysLog("微信公众号添加应聘手机号")
	@PostMapping("/wechat/mobile/add")
	public Result<Boolean> addMobileFromWechat(@RequestBody SaveWechatApplicationReqDTO saveWechatApplicationReqDTO) {
		SaveWechatApplicationDTO saveApplicationDTO = new SaveWechatApplicationDTO();
		BeanUtils.copyProperties(saveWechatApplicationReqDTO,saveApplicationDTO);
		return smtApplictionService.addMobileFromWechat(saveApplicationDTO);
	}

	/**
	 * 微信关照添加应聘者人脸照片
	 *
	 * @return Result
	 */
	@SysLog("微信公众号添加应聘手机号")
	@PostMapping("/wechat/face/add")
	public Result addFaceFromWechat(@RequestBody SaveWechatApplicationReqDTO saveWechatApplicationReqDTO) {
		SaveWechatApplicationDTO saveApplicationDTO = new SaveWechatApplicationDTO();
		BeanUtils.copyProperties(saveWechatApplicationReqDTO,saveApplicationDTO);
		return smtApplictionService.addFaceFromWechat(saveApplicationDTO);
	}

	/**
	 *微信公众号 通过id查询应聘表详情
	 *
	 * @param id
	 *            id
	 * @return Result
	 */
	@GetMapping("/wechat/application/simple/info/{id}")
	public Result<SmtApplicationRespDTO> getSimpleInfo(@PathVariable("id") Long id) {
		SmtApplication smtApplication = smtApplictionService.getSimpleInfo(id);
		SmtApplicationRespDTO smtApplicationRespDTO = new SmtApplicationRespDTO();
		BeanUtils.copyProperties(smtApplication,smtApplicationRespDTO);
		return success(smtApplicationRespDTO);
	}

	/**
	 * 批量修改应聘表
	 *
	 * @return Result
	 */
	@SysLog("批量修改应聘表")
	@PostMapping("updateApplicationList")
	public Result updateApplication(@RequestBody UpApplicationListReqDTO upApplicationListReqDTO) {
		UpApplicationListDTO upApplicationListDTO = new UpApplicationListDTO();
		BeanUtils.copyProperties(upApplicationListReqDTO,upApplicationListDTO);
		return smtApplictionService.updateApplicationList(upApplicationListDTO);
	}


	/**
	 * 重新同步
	 *
	 * @return Result
	 */
	@SysLog("重新同步")
	@PostMapping("updateApplicationToStaff")
	public Result updateApplicationToStaff(@RequestBody UpApplicationListDTO application) {
		return smtApplictionService.updateApplicationToStaff(application);
	}

	/**
	 * 通过id删除应聘表
	 *
	 * @param id
	 *            id
	 * @return Result
	 */
	@SysLog("删除应聘表")
	@PostMapping("/{id}")
	public Result removeById(@PathVariable("id") Long id) {
		return smtApplictionService.removeApplicationById(id);
	}


	/**
	 * 通过人脸照片查询应聘信息
	 * @param facePhoto
	 * @return
	 */
	@SysLog("app接口通过人脸招聘搜索简历")
	@PostMapping("/face")
	public Result<FaceApplicationVO> getByface(@RequestBody String facePhoto)
	{
		return smtApplictionService.getByface(facePhoto);
	}
	/**
	 * 查询应聘流程
	 * @param id 应聘id
	 * @return
	 */
	@SysLog("查询应聘流程")
	@GetMapping("/getApplicationProcess")
	public Result<List<SmtApplicationProcess>> getProcess( @RequestParam("id") Long id)
	{
		return success(smtApplictionService.getApplicationProcess(id));
	}



	@SysLog("app接口查询应聘者列表")
	@Inner
	@PostMapping("/list")
	public Result<IPage<ApplicationListRespDTO>> getSmtApplictionList(Page page, @RequestBody ApplicationListDTO applicationDTO) {
		//IPage<ApplicationListVO> pageRs = smtApplictionService.getSmtApplictionList(page, applicationDTO);
		return  success(smtApplictionService.getSmtApplictionList(page, applicationDTO),ApplicationListRespDTO.class);
	}


	@SysLog("app接口查询所有发布的岗位")
	@GetMapping("/job/list")
	public Result getJobList(@RequestParam("parkId") Integer parkId, @RequestParam(value = "jobName", required=false) String jobName) {
		return new Result<>(smtApplictionService.getJobList(parkId, jobName));
	}

	@SysLog("app接口查看简历详情")
	@Inner
	@GetMapping("/getById")
	public Result<ApplicationInfoRespDTO> getApplicationById(@RequestParam("id") String id) {
		ApplicationInfoVO applicationInfoVO = smtApplictionService.getApplictionById(id);
		ApplicationInfoRespDTO applicationInfoRespDTO = new ApplicationInfoRespDTO();
		BeanUtils.copyProperties(applicationInfoVO, applicationInfoRespDTO);
		return success(applicationInfoRespDTO);
	}

	/*
	 * 投递
	 */
	@SysLog("app接口执行投递操作")
	@GetMapping("/deliver")
	public Result<Boolean> delivery(@RequestParam("applicationId") Long applicationId, @RequestParam("maritalStatus") Integer maritalStatus ) {
		return smtApplictionService.delivery(applicationId,maritalStatus);
	}



	@SysLog("app接口查询应聘流程")
	@Inner
	@GetMapping("/app/getApplicationProcess")
	public Result<List<SmtApplicationProcessRespDTO>> getApplicationProcess( @RequestParam("id") Long id){
		List<SmtApplicationProcess> tempList = smtApplictionService.getApplicationProcess(id);
		return success(tempList, SmtApplicationProcessRespDTO.class);
	}



	@SysLog("app接口批量修改应聘表")
	@Inner
	@PostMapping("/app/updateApplicationList")
	public Result<Boolean> updateApplicationList(@RequestBody UpApplicationListReqDTO upApplicationListReqDTO) {
		UpApplicationListDTO application = new UpApplicationListDTO();
		BeanUtils.copyProperties(upApplicationListReqDTO,upApplicationListReqDTO);
		return smtApplictionService.updateApplicationList(application);
	}


}
