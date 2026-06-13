package com.tce.smart.app.service.wechat;

import com.tce.smart.app.ao.fore.FamilyMemberAddAO;
import com.tce.smart.app.ao.fore.OrgrelationAddAO;
import com.tce.smart.app.ao.fore.RelationAo;
import com.tce.smart.app.ao.wechat.*;
import com.tce.smart.app.vo.fore.DicContentVo;
import com.tce.smart.app.vo.fore.EmployeeVo;
import com.tce.smart.app.vo.wechat.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.SmtApplicationEmailDTO;
import com.tce.smart.platform.api.dto.SmtParkDTO;
import com.tce.smart.platform.api.dto.SmtRecruitmentDTO;
import com.tce.smart.platform.api.dto.req.ApplicationEmergencyReqDTO;
import com.tce.smart.platform.api.dto.resp.EducationRespDTO;
import com.tce.smart.platform.api.dto.resp.JobListRespDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 招聘信息
 *
 * @author mingkai.wu
 * @date 2019-05-10 16:16:16
 */
public interface JobService {

	/**
	 * 获取园区列表
	 *
	 * @return
	 */
	List<SmtParkDTO> getParkList();

	/**
	 * 获取岗位列表
	 *
	 * @param smtRecruitment
	 * @return GetJobListVo
	 */
	List<JobListRespDTO> getJobList(SmtRecruitmentDTO smtRecruitment);

	/**
	 * 获取园区详情
	 *
	 * @param applicationId 招聘岗位Id
	 * @return
	 */
	JobDetailVo getJobDetail(String applicationId);

	/**
	 * 读取身份证照片信息
	 *
	 * @param ocrReadCardImgAo 身份证照片信息
	 * @param openId 微信用户id
	 * @return
	 */
	OcrReadCardImgVo readCardImg(OcrReadCardImgAo ocrReadCardImgAo,String openId);

	/**
	 * 提交人脸照片
	 *
	 * @param addJobFaceAo 人脸照片信息
	 * @return
	 */
	Boolean addFaceImg(AddJobFaceAo addJobFaceAo);

	List<EducationRespDTO> getEducationHis(String applicationId);

	List getWorkHis(String applicationId);

	Result addEducationHis(EducationHisAo educationAo);

	Result addWorkHis(WorkHisAo workAo);

	/**
	 * 绑定手机号
	 *
	 * @param verifySmsCodeAo
	 * @return
	 */
	Boolean bindMobile(VerifySmsCodeAo verifySmsCodeAo);

	/**
	 *
	 * @param applicationId 应聘Id
	 * @return
	 */
	Boolean submitApplication(String applicationId, Integer maritalStatus);

	/**
	 * 获取学位列表
	 * @return
	 */
	List<DicContentVo> getDegreeType();

	/**
	 * 获取学历利列表
	 * @return
	 */
	List<DicContentVo> getEducationType();

	Result attachmentSubmit(MultipartFile file,  String applicationId);


	void applicationRelationUpdate(ApplicationEmergencyReqDTO relationAo);



	/**
	 * 添加紧急联系人
	 * @param relationAo
	 * @return
	 */
	Result relationAdd(RelationAo relationAo);

	/**
	 * 添加家庭成员
	 * @param familyMemberAddAO
	 */
	void familySave(FamilyMemberAddAO familyMemberAddAO);

	/**
	 * 添加人事关系
	 * @param  orgrelationAddAO
	 */
	void orgrelationSave(OrgrelationAddAO orgrelationAddAO);

	/**
	 * 根据工号获取紧急联系人
	 * @param applicationId
	 */
	RelationVo relationsGet(String applicationId);

	/**
	 * 获取家庭成员
	 * @param applicationId
	 * @return
	 */
	List<FamilyMemberVO> familyGet(String applicationId);

	/**
	 * 获取人事关系
	 * @param applicationId
	 * @return
	 */
	List<OrgrelationVo> orgrelationGet(String applicationId);



	/**
	 * 获取关系类型列表
	 * @return
	 */
	List<RelationTypeVO> relationList();

	String saveCardInfo(OcrReadCardImgVo ocrReadCardImgAo, String openId);

	SmtApplicationEmailDTO emailGet(String applicationId);

	void emailAdd(ApplicationEmailAo email);

	Result emailUpdate(ApplicationEmailAo email);

	EmployeeVo getBaseinfo(String badge);


	/**
	 * 获取紧急联系人关系字典
	 * @return
	 */
	List<RelationTypeVO> emergencyRelationList();

}
