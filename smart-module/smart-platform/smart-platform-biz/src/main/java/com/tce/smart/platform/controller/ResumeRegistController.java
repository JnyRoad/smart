package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.resp.EducationRespDTO;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.entity.SmtApplicationFamily;
import com.tce.smart.platform.core.vo.OcrReadCardImgVO;
import com.tce.smart.platform.service.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 简历登记控制器
 * @author QIPEI
 *
 */
@RestController
@AllArgsConstructor
@RequestMapping("/regist")
public class ResumeRegistController extends BaseController {


	@Autowired
	private  ResumeRegistService service;


	@Autowired
	private  SmtApplicationEducationService smtApplicationEducationService;

	@Autowired
	private SmtApplicationEmergencyService  smtApplicationEmergencyService;

	@Autowired
	private SmtApplicationFamilyService smtApplicationFamilyService;


	@Autowired
	private SmtApplicationWorkService smtApplicationWorkService;

	@Autowired
	private SmtApplicationRelationService  smtApplicationRelationService;

	@Autowired
	private ResumeFaceCropCapabilityService resumeFaceCropCapabilityService;
	/**
	 * 获取字典关系
	 * @return
	 */
	@GetMapping("/relation/list")
	public Result relationList(){
		return  new Result<>(service.relationList());
	}

	@GetMapping("/emergency/relation/list")
	public Result emergencyRelationList(){
		return  new Result<>(service.emergencyRelationList());
	}

	@GetMapping("/degree/type")
	public Result getDegreeType() {
		return new Result<>(service.getDegreeType());
	}


	/**
	 * 获取学历类型
	 *
	 * @return
	 */
	@GetMapping("/education/type")
	public Result getEducationType() {
		return new Result<>(service.getEducationType());
	}

	@SysLog("通过其他应聘网站录入招聘数据，证件识别")
	@PostMapping("/ocrRead")
	public Result readCardImg(@RequestBody OcrReadCardImgDTO ocrReadCardImgDTO) {
		return  new Result<>(service.readCardImg(ocrReadCardImgDTO));
	}


	/**
	 * 保存身份证信息
	 * @param ocrReadCardImgVo
	 * @return
	 */
	@SysLog("通过其他应聘网站录入招聘数据，保存证件信息")
	@PostMapping("/save/identification")
	public Result saveCardInfo(@RequestBody OcrReadCardImgVO ocrReadCardImgVo, HttpServletResponse response) {
		String applicationId = service.saveCardInfo(ocrReadCardImgVo);
		resumeFaceCropCapabilityService.issueCropCapability(response, Long.valueOf(applicationId));
		return  new Result<>(applicationId);
	}


	/**
	 * 提交人脸照片
	 *
	 * @param addJobFaceDTO 人脸照片
	 * @return Result
	 */
	@SysLog("通过其他应聘网站录入招聘数据，提交人脸照片")
	@PostMapping("/face/add")
	public Result<Integer> addFaceImg(@CookieValue(value = "resume_face_save", required = false) String capability,
			@RequestBody AddJobFaceDTO addJobFaceDTO) {
		if (addJobFaceDTO == null) {
			throw new AccessDeniedException("简历人脸处理上下文无效或已过期");
		}
		try {
			resumeFaceCropCapabilityService.consumeSaveCapability(capability,
					Long.valueOf(addJobFaceDTO.getApplicationId()), addJobFaceDTO.getFacePhoto());
		} catch (NumberFormatException e) {
			throw new AccessDeniedException("简历人脸处理上下文无效或已过期");
		}
		return new Result<>(service.addFaceImg(addJobFaceDTO));
	}

	/**
	 * 投递
	 * @param applicationId
	 * @return
	 */
	@GetMapping("/deliver")
	public Result deliveryThird(@RequestParam("applicationId") String applicationId ) {
		return service.deliveryThird(Long.parseLong(applicationId));
	}


	@GetMapping("/send/{mobile}")
	public Result sendSmsCode(@PathVariable String mobile) {
		return new Result<>(service.sendSmsCode(mobile));
	}

	/**
	 * 绑定手机号
	 *
	 * @param verifySmsCodeAo
	 * @return
	 */
	@PostMapping("/bind/mobile")
	public Result bindMobile(@RequestBody VerifySmsCodeDTO verifySmsCodeAo) {
		return new Result<>(service.bindMobile(verifySmsCodeAo));
	}



	  @SysLog("第三方招聘网站同步数据，获取教育经历")
	  @GetMapping("/education/list/{applicationId}")
	  public Result<List<EducationRespDTO>> getSmtApplicationEducation(@PathVariable("applicationId") String applicationId) {
	    return  success(smtApplicationEducationService.getSmtApplicationEducationList(applicationId),EducationRespDTO.class);
	  }

	  @SysLog("第三方招聘网站同步数据，添加教育经历")
	  @PostMapping("/addApplicationeEducation")
	  public Result addApplicationeEducationThird(@RequestBody ApplicationEducationDTO applicationEducationDTO){
	    return service.saveEducationThird(applicationEducationDTO);
	  }

	  @SysLog("第三方招聘网站同步数据，修改教育经历")
	  @PostMapping("/updateApplicationeEducation")
	  public Result updateApplicationeEducation(@RequestBody ApplicationEducationDTO applicationEducationDTO){
	    return service.updateApplicationeEducation(applicationEducationDTO);
	  }


	  @SysLog("删除教育经历")
	  @GetMapping("/deleteEducation/{id}")
	  public Result deleteEducation(@PathVariable Integer id){
		return  new Result<>(smtApplicationEducationService.removeById(id));
	  }



	  /**
	   * 通过id查询应聘者紧急联系人
	   * @param applicationId
	   * @return Result
	   */
	  @GetMapping("/getEmployee/{applicationId}")
	  public Result getByEmployeeIdThird(@PathVariable String applicationId){
	    Result result = Result.builder().build();
	    try {
	      result =  service.getByApplicationIdThird(applicationId);
	    } catch (Exception e) {
	      result.setMsg("通过应聘者id查询紧急联系人出错");
	    }
	    return result;
	  }


	 @SysLog("新增应聘者紧急联系人")
	 @PostMapping("/addApplicationEmergency")
	 public Result addApplicationEmergencyThird(@RequestBody ApplicationEmergencyDTO emergencyDTO){
	    return service.saveApplicationEmergencyThird(emergencyDTO);
	 }


	@SysLog("修改应聘者紧急联系人")
	@PostMapping("/updateEmergency")
	public Result<Integer> updateByBadgeThird(@RequestBody ApplicationEmergencyDTO emergencyDTO){
		 return success(smtApplicationEmergencyService.updateByIdApplicationEmergency(emergencyDTO));
	}

	@SysLog("删除应聘者紧急联系人")
	@GetMapping("/deleteEmergency/{id}")
	public Result deleteEmergency(@PathVariable Integer id){
		 return  new Result<>(smtApplicationEmergencyService.removeById(id));
	}



	@GetMapping("/family/list/{applicationId}")
	public Result getByApplicationIdThird(@PathVariable String applicationId){
	    return success(smtApplicationFamilyService.getByApplicationId(applicationId));
	 }


	 @SysLog("新增应聘者家庭成员表 ")
	 @PostMapping("/addApplicationFamily")
	 public Result saveThird(@RequestBody ApplicationFamilyDTO applicationFamilyDTO){
	    return service.saveThird(applicationFamilyDTO);
	 }

	 @SysLog("新增应聘者家庭成员表 ")
	 @PostMapping("/updateApplicationFamily")
	 public Result updateThird(@RequestBody ApplicationFamilyDTO applicationFamilyDTO){
	    return service.updateThird(applicationFamilyDTO);
	 }

	@SysLog("删除应聘者家庭成员表")
	@GetMapping("/deleteFamily/{id}")
	public Result deleteFamily(@PathVariable Integer id){
		 return  new Result<>(smtApplicationFamilyService.removeById(id));
	}


	 @SysLog("第三方招聘同步数据，获取工作经历")
	 @GetMapping("/work/list/{applicationId}")
	 public Result getSmtApplicationWorkListThird(@PathVariable("applicationId") String applicationId ) {
	    return  new Result<>(smtApplicationWorkService.getSmtApplicationWorkList(applicationId));
	 }

	 @SysLog("第三方招聘同步数据，获取人事关系")
	 @GetMapping("/relation/list/{applicationId}")
	 public Result getSmtApplicationRelationListThird(@PathVariable("applicationId") String applicationId ) {

	    return  new Result<>(smtApplicationRelationService.getByApplicationId(applicationId));
	 }





	 @SysLog("新增应聘者家庭成员表 ")
	 @PostMapping("/updateApplicationWork")
	 public Result updateApplicationWorkThird(@RequestBody ApplicationWorkDTO applicationWorkDTO){
	    return service.updateApplicationWorkThird(applicationWorkDTO);
	 }

	 @SysLog("第三方招聘同步数据，添加工作经验")
	 @PostMapping("/addApplicationWork")
	 public Result addApplicationWorkThird(@RequestBody ApplicationWorkDTO applicationWorkDTO){

	    return service.addApplicationWorkThird(applicationWorkDTO);
	 }


	@SysLog("删除工作经验")
	@GetMapping("/deleteWork/{id}")
	public Result deleteWork(@PathVariable Integer id){
			 return  new Result<>(smtApplicationWorkService.removeById(id));
	}

}
