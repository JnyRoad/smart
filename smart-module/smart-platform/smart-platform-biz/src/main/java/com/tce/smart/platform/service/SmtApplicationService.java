package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.entity.SmtApplication;
import com.tce.smart.platform.core.entity.SmtApplicationProcess;
import com.tce.smart.platform.core.vo.*;

import java.util.List;

/**
 * 应聘表
 *
 * @author 齐佩
 * @date 2019-04-13 18:18:24
 */
public interface SmtApplicationService extends IService<SmtApplication> {

	/**
	 * 微信公众号添加应聘信息
	 *
	 * @param saveApplicationDTO
	 * @return 应聘id
	 */
	Result<Long> addWechatAppliction (SaveWechatApplicationDTO saveApplicationDTO);

	Result addAppliction(AddOrUpApplicationDTO application);

	Result updateApplicationById(AddOrUpApplicationDTO application);

	Result removeApplicationById(Long id);

	IPage<ApplicationVO> getSmtApplictionPage(Page page, ApplicationDTO applicationDTO, String rangTime,String ageRang, List<Integer> parkIds);

	ApplicationInfoVO getApplictionById(String id);

	List<SmtApplicationProcess> getApplicationProcess(Long id);

	Result<Boolean> updateApplicationList(UpApplicationListDTO application);

	Result<FaceApplicationVO> getByface(String facePhoto);

	Result getApplicationResume(Long id);


	IPage<ApplicationListVO> getSmtApplictionList(Page page, ApplicationListDTO applicationDTO);

	List<JobVO> getJobList(Integer parkId, String deptName);

	Result<Boolean> delivery(Long applicationId,Integer maritalStatus);

	/**
	 * 添加应聘者手机号
	 * @param saveApplicationDTO
	 * @return
	 */
	Result<Boolean> addMobileFromWechat(SaveWechatApplicationDTO saveApplicationDTO);

	/**
	 * 获取应聘者主要信息
	 * @param id
	 * @return
	 */
	SmtApplication getSimpleInfo(Long id);

	/**
	 * 微信关照添加应聘者人脸照片
	 *
	 * @param saveApplicationDTO
	 * @return
	 */
	Result<?> addFaceFromWechat(SaveWechatApplicationDTO saveApplicationDTO);

	/**
	 * 根据身份证号查询应聘信息
	 *
	 * @param certno 身份证号
	 * @param recruitId 招聘岗位id
	 * @return
	 */
	SmtApplication getByIdCardNo(String certno,Integer recruitId);

	/**
	 * 后台获取流程
	 * @param id
	 * @return
	 */
	Result getProcess(String id);

	Result getApplictionInfoById(String id);
	/**
	 * 重新同步
	 * @param application
	 * @return
	 */
	Result updateApplicationToStaff(UpApplicationListDTO application);

}
