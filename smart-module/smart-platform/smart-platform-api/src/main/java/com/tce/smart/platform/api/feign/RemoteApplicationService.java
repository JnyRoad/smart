package com.tce.smart.platform.api.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.*;
import com.tce.smart.platform.api.dto.req.*;
import com.tce.smart.platform.api.dto.resp.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 应聘者管理
 * @author 齐佩
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteApplicationService {

	@PostMapping("/application/wechat/add")
	Result addWechatAppliction(@RequestBody SaveWechatApplicationReqDTO saveApplicationDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 微信公招号添加手机号
	 * @param saveApplicationDTO
	 * @return
	 */
	@PostMapping("/application/wechat/mobile/add")
	Result<?> addMobileFromWechat(@RequestBody SaveWechatApplicationReqDTO saveApplicationDTO,@RequestHeader(SecurityConstants.FROM) String from) ;

	/**
	 * 微信关照添加应聘者人脸照片
	 *
	 * @param saveApplicationDTO
	 * @return
	 */
	@PostMapping("/application/wechat/face/add")
	Result<?> addFaceFromWechat(@RequestBody SaveWechatApplicationReqDTO saveApplicationDTO,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 查询应聘者详情
	 * @param id
	 * @param from
	 * @return
	 */
	@GetMapping("/application/wechat/application/simple/info/{id}")
	Result<SmtApplicationRespDTO> getSimpleInfo(@RequestParam("id") final Long id, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 查看应聘列表
	 * @param current
	 * @param size
	 * @param applicationListReqDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/application/list")
	Result<Page<ApplicationListRespDTO>> getSmtApplictionList(@RequestParam("current") final Integer current, @RequestParam("size") final Integer size, @RequestBody ApplicationListReqDTO applicationListReqDTO, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 查看应聘详情
	 * @param id
	 * @return
	 */
	@GetMapping("/application/getById")
	Result<ApplicationInfoRespDTO> getApplicationById(@RequestParam("id") String id,@RequestHeader(SecurityConstants.FROM) String from);



	/**
	 * 查看某个岗位流程
	 * @param id
	 * @return
	 */
	@GetMapping("/application/app/getApplicationProcess")
	Result<List<SmtApplicationProcessRespDTO>> getApplicationProcess(@RequestParam("id") Long id,@RequestHeader(SecurityConstants.FROM) String from);


	/**
	 *
	 * 批量修改应聘者信息
	 * @param application
	 * @return
	 */
	@PostMapping("/application/app/updateApplicationList")
     Result updateApplicationList(@RequestBody UpApplicationListReqDTO application, @RequestHeader(SecurityConstants.FROM) String from);



	/**
	 * 获取正在招聘中的岗位
	 * @return
	 */
	@GetMapping("/application/job/list/{parkId}")
	Result JobList(@PathVariable("parkId") Integer parkId ,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 人脸搜索简历
	 * @param facePhoto
	 * @return
	 */
	@PostMapping("/application/face")
	Result<FaceApplicationRespDTO> getByface(@RequestBody String facePhoto, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 查询教育经历
	 * @param applicationId applicationId
	 * @param from from
	 * @return
	 */
	@GetMapping("/application/education/list/{applicationId}")
	Result<List<EducationRespDTO>> getSmtApplicationEducationList(@PathVariable("applicationId") String applicationId, @RequestHeader(SecurityConstants.FROM) String from);



	/**
	 * 查询工作经历
	 * @param applicationId applicationId
	 * @param from from
	 * @return
	 */
	@GetMapping("/application/work/list/{applicationId}")
	Result<List<SmtApplicationWorkRespDTO>> getSmtApplicationWorkList(@PathVariable("applicationId") String applicationId, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 添加教育經驗
	 * @param applicationEducation
	 * @return
	 */
	@PostMapping("/application/education/addApplicationeEducation")
	Result addApplicationeEducation(@RequestBody SmtApplicationEducationDTO applicationEducation, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 添加工作經驗
	 * @param applicationWork
	 * @return
	 */
	@PostMapping("/application/work/addApplicationWork")
	Result addApplicationWork(@RequestBody SmtApplicationWorkDTO applicationWork, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 *
	 * @param applicationId applicationId
	 * @param maritalStatus maritalStatus
	 * @param from from
	 * @return
	 */
	@GetMapping("/application/deliver")
	Result delivery(@RequestParam("applicationId") Long applicationId, @RequestParam("maritalStatus") Integer maritalStatus,@RequestHeader(SecurityConstants.FROM) String from);



	/**
	 * 删除工作经历
	 * @param applicationId applicationId
	 * @param from from
	 * @return
	 */
	@GetMapping("/application/work/delete/{applicationId}")
	Result<Boolean> deleteApplicationWorkList(@PathVariable("applicationId") String applicationId,@RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 删除教育经历
	 * @param applicationId applicationId
	 * @param from from
	 * @return
	 */
	@GetMapping("/application/education/delete/{applicationId}")
	Result<Integer>  deleteEducationList(@PathVariable("applicationId") String applicationId,@RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 上传简历文件
	 * @param smtApplicationResume
	 * @param from
	 * @return
	 */
	@PostMapping("/application/resume/addApplicationResume")
	Result save(@RequestBody SmtApplicationResumeDTO smtApplicationResume, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 修改简历文件
	 * @param file 简历文件
	 * @param smtApplicationResume
	 * @param from
	 * @return
	 */
	@PostMapping("/application/resume/updateApplicationResume")
	Result updateById(@RequestParam("resume") MultipartFile file, @RequestBody SmtApplicationResumeDTO smtApplicationResume, @RequestHeader(SecurityConstants.FROM) String from);



	/**
	 * 新增紧急联系人
	 * @param smtApplicationEmergency
	 * @return
	 */
	@PostMapping("/application/emergency/addApplicationEmergency")
	Result<Boolean>  addApplicationEmergency(@RequestBody SmtApplicationEmergencyDTO smtApplicationEmergency, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 新增家庭成员
	 * @param smtApplicationFamily
	 * @return
	 */
	@PostMapping("/application/family/addApplicationFamily")
	Result<Boolean> addApplicationFamily(@RequestBody SmtApplicationFamilyDTO smtApplicationFamily, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 添加人事关系
	 * @param smtApplicationRelation
	 * @return
	 */
	@PostMapping("/application/relation/addApplicationRelation")
	Result addApplicationRelation(@RequestBody SmtApplicationRelationDTO smtApplicationRelation, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 获取紧急联系人
	 * @param applicationId
	 * @return
	 */
	@GetMapping("/application/emergency/getByApplicationId/{applicationId}")
	Result<SmtApplicationEmergencyDTO> getApplicationEmergency(@RequestParam("applicationId") String applicationId, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 获取家庭成员
	 * @param applicationId
	 * @return
	 */
	@GetMapping("/application/family/getByApplicationId/{applicationId}")
	Result<List<SmtApplicationFamilyDTO>> getApplicationFamily(@RequestParam("applicationId") String applicationId, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 获取人事关系
	 * @param applicationId
	 * @return
	 */
	@GetMapping("/application/relation/getByApplicationId/{applicationId}")
	Result<List<SmtApplicationRelationDTO>> getApplicationRelation(@RequestParam("applicationId") String applicationId, @RequestHeader(SecurityConstants.FROM) String from);

	/*@GetMapping("")
	Result<List<SmtApplicationRelation>> getRelationDict();*/

	/**
	 * 删除家庭成员
	 * @param applicationId applicationId
	 * @param from from
	 * @return
	 */
	@GetMapping("/application/family/deleteFamily/{applicationId}")
	Result removeFamilyByApplicationId(@PathVariable("applicationId") Long applicationId,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 删除人事关系
	 * @param applicationId
	 * @param from
	 * @return
	 */
	@GetMapping("/application/relation/deleteRelation/{applicationId}")
	Result removeRelationByApplicationId(@PathVariable("applicationId") Long applicationId,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 修改紧急联系人
	 * @param applicationEmergencyDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/application/emergency/updateByApplication")
	Result<Integer> updateByIdApplicationEmergency(@RequestBody ApplicationEmergencyReqDTO applicationEmergencyDTO, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 添加邮箱
	 * @param email
	 * @param from
	 * @return
	 */
	@PostMapping("/application/email/add")
	Result addApplicationEmailList(@RequestBody SmtApplicationEmailDTO email, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 修改邮箱
	 * @param email
	 * @param from
	 * @return
	 */
	@PostMapping("/application/email/update")
	Result updateApplicationEmailList(@RequestBody SmtApplicationEmailDTO email ,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 查询应聘者的邮箱
	 * @param applicationId
	 * @param from
	 * @return
	 */
	@GetMapping("/application/email/list/{applicationId}")
	Result<SmtApplicationEmailDTO> getSmtApplicationEmailList(@PathVariable("applicationId") Long applicationId,@RequestHeader(SecurityConstants.FROM) String from);


}
