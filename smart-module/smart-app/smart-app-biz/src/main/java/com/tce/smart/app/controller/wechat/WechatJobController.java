package com.tce.smart.app.controller.wechat;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.ao.fore.FamilyMemberAddAO;
import com.tce.smart.app.ao.fore.OrgrelationAddAO;
import com.tce.smart.app.ao.fore.RelationAo;
import com.tce.smart.app.ao.wechat.*;
import com.tce.smart.app.service.wechat.JobService;
import com.tce.smart.app.vo.wechat.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.SmtRecruitmentDTO;
import com.tce.smart.platform.api.dto.req.ApplicationEmergencyReqDTO;
import com.tce.smart.platform.api.dto.resp.JobListRespDTO;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 微信公众号招聘控制器
 *
 * @author mingkai.wu
 * @date 2019-05-13 09:25:37
 */
@RestController
@AllArgsConstructor
@RequestMapping("/wechat/job")
public class WechatJobController extends BaseController {

	private JobService jobService;

	/**
	 * 获取岗位列表
	 *
	 * @param
	 * @return
	 */
	@GetMapping("/park/list")
	public Result getPakrList() {
		return success(jobService.getParkList());
	}


	/**
	 * 获取岗位列表
	 *
	 * @param
	 * @return
	 */
	@GetMapping("/list/{parkId}")
	public Result getJobList(@PathVariable Integer parkId, Page pageParam) {

		SmtRecruitmentDTO smtRecruitment=new SmtRecruitmentDTO();
		smtRecruitment.setParkId(parkId);

		List<JobListRespDTO> page = jobService.getJobList(smtRecruitment);
		return success(page);
	}

	/**
	 * 查看岗位招聘信息详情
	 *
	 * @param recruitId
	 * @return
	 */
	@GetMapping("/detail/{recruitId}")
	public Result getRecruitDetail(@PathVariable String recruitId) {
		return success(jobService.getJobDetail(recruitId));
	}

	/**
	 * 读取身份证照片信息
	 *
	 * @param ocrReadCardImgAo
	 * @param openId           微信用户openId
	 * @return
	 */
	@PostMapping("/ocr/identification")
	public Result readCardImg(@RequestBody OcrReadCardImgAo ocrReadCardImgAo, @RequestHeader(value = "openId", required = true) String openId) {
		return success(jobService.readCardImg(ocrReadCardImgAo, openId));
	}


	/**
	 * 保存身份证信息
	 * @param ocrReadCardImgAo
	 * @param openId
	 * @return
	 */
	@PostMapping("/ocr/save/identification")
	public Result saveCardInfo(@RequestBody OcrReadCardImgVo ocrReadCardImgAo, @RequestHeader(value = "openId", required = true) String openId) {
		return success(jobService.saveCardInfo(ocrReadCardImgAo, openId));
	}




	/**
	 * 提交人脸照片
	 *
	 * @param addJobFaceAo 人脸照片
	 * @return Result
	 */
	@PostMapping("/face/add")
	public Result addFaceImg(@RequestBody AddJobFaceAo addJobFaceAo) {
		return success(jobService.addFaceImg(addJobFaceAo));
	}

	/**
	 * 绑定手机号
	 *
	 * @param verifySmsCodeAo
	 * @return
	 */
	@PostMapping("/bind/mobile")
	public Result bindMobile(@RequestBody VerifySmsCodeAo verifySmsCodeAo) {
		return success(jobService.bindMobile(verifySmsCodeAo));
	}

	/**
	 * 投递简历
	 *
	 * @param applicationId 应聘Id
	 * @return Result
	 */
	@GetMapping("/application/submit")
	public Result delivery(@RequestParam("applicationId") String applicationId, @RequestParam("maritalStatus") Integer maritalStatus) {
		return success(jobService.submitApplication(applicationId,maritalStatus));
	}

	/**
	 * 获取教育经历
	 *
	 * @param applicationId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@GetMapping("/application/educationHis/list/{applicationId}")
	public Result educationHis(@PathVariable String applicationId) {
		return success(jobService.getEducationHis(applicationId));
	}


	/**
	 * 获取工作经验
	 *
	 * @param applicationId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@GetMapping("/application/workHis/list/{applicationId}")
	public Result workHis(@PathVariable String applicationId) {
		return success(jobService.getWorkHis(applicationId));
	}

	/**
	 * 提交教育经验
	 * @param educationAo
	 * @return
	 */
	@PostMapping("/application/educationHis/submit")
	public Result addEducationHis(@RequestBody EducationHisAo educationAo) {
		return jobService.addEducationHis(educationAo);
	}

	/**
	 * 提交工作经验
	 * @param workAo
	 * @return
	 */
	@PostMapping("/application/workHis/submit")
	public Result addWorkHis(@RequestBody WorkHisAo workAo) {
		return jobService.addWorkHis(workAo);

	}

	/**
	 * 获取学位类型
	 *
	 * @return
	 */
	@GetMapping("/degree/type")
	public Result getDegreeType() {
		return new Result<>(jobService.getDegreeType());
	}


	/**
	 * 获取学历类型
	 *
	 * @return
	 */
	@GetMapping("/education/type")
	public Result getEducationType() {
		return new Result<>(jobService.getEducationType());
	}

	/**
	 * 简历文件的提交
	 * @param file
	 * @param applicationId
	 * @return
	 */
	@PostMapping("/application/attachment/submit")
	public Result attachmentSubmit(@RequestParam ("attachFile") MultipartFile file, @RequestParam ("applicationId") String applicationId) {
		return jobService.attachmentSubmit(file, applicationId);
	}



	/**
	 * 更新应聘者紧急联系人
	 * @param relationAo
	 * @return
	 */
	@PostMapping("/emergency/update")
	public Result applicationRelationUpdate(@RequestBody ApplicationEmergencyReqDTO relationAo)
	{
		jobService.applicationRelationUpdate(relationAo);
		return success();
	}



	/**
	 * 添加紧急联系人
	 * @param relationAo
	 * @return
	 */
	@PostMapping("/relation/add")
	public Result relationAdd(@RequestBody RelationAo relationAo)
	{
		return jobService.relationAdd(relationAo);

	}

	/**
	 * 添加家庭成员
	 * @param familyMemberAddAO
	 * @return
	 */
	@PostMapping("/family/save")
	public Result familySave(@RequestBody FamilyMemberAddAO familyMemberAddAO)
	{
		jobService.familySave(familyMemberAddAO);
		return success();
	}

	/**
	 * 添加人事关系
	 * @param orgrelationAddAO
	 * @return
	 */
	@PostMapping("/orgrelation/save")
	public Result orgrelationSave(@RequestBody OrgrelationAddAO orgrelationAddAO)
	{
		jobService.orgrelationSave(orgrelationAddAO);
		return success();
	}

	/**
	 * 获取紧急联系ren
	 * @param applicationId
	 * @return
	 */
	@GetMapping("/relations/get/{applicationId}")
	public Result relationsGet(@PathVariable("applicationId") String applicationId){
		RelationVo relation = jobService.relationsGet(applicationId);
		return success(relation);
	}
	/**
	 * 获取家庭成员
	 * @param applicationId
	 * @return
	 */
	@GetMapping("/family/get/{applicationId}")
	public Result familyGet(@PathVariable("applicationId") String applicationId){
		List<FamilyMemberVO> list = jobService.familyGet(applicationId);
		return success(list);
	}

	/**
	 * 获取人事关系
	 * @param applicationId
	 * @return
	 */
	@GetMapping("/orgrelation/get/{applicationId}")
	public Result orgrelationGet(@PathVariable("applicationId") String applicationId){
		List<OrgrelationVo> list = jobService.orgrelationGet(applicationId);
		return success(list);
	}

	/**
	 * 获取字典关系
	 * @return
	 */
	@GetMapping("/relation/list")
	public Result relationList(){
		List<RelationTypeVO> list = jobService.relationList();
		return success(list);
	}


	/**
	 * 获取字典关系
	 * @return
	 */
	@GetMapping("/emergency/relation/list")
	public Result emergencyRelationList(){
		List<RelationTypeVO> list = jobService.emergencyRelationList();
		return success(list);
	}


	/**
	 * 获取邮箱
	 * @param applicationId
	 * @return
	 */
	@GetMapping("/email/get/{applicationId}")
	public Result emailGet(@PathVariable("applicationId") String applicationId){
		return success(jobService.emailGet(applicationId));
	}


	/**
	 * 获取邮箱
	 * @param email email
	 * @return
	 */
	@PostMapping("/email/add")
	public Result emailAdd(@RequestBody ApplicationEmailAo email){
		jobService.emailAdd(email);
		return success();
	}

	/**
	 * 获取邮箱
	 * @param email email
	 * @return
	 */
	@PostMapping("/email/update")
	public Result emailUpdate(@RequestBody ApplicationEmailAo email){

		return jobService.emailUpdate(email);
	}



	/**
	 * 获取员工基本信息
	 * @param badge 员工工号
	 * @return
	 */
	@GetMapping("/baseinfo/{badge}")
	public Result getBaseinfo(@PathVariable("badge") String badge)
	{
		return success(jobService.getBaseinfo(badge));
	}


}
