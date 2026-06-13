package com.tce.smart.platform.service;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.dto.*;
import com.tce.smart.platform.core.vo.DicContentVO;
import com.tce.smart.platform.core.vo.OcrReadCardImgVO;

import java.util.List;

public interface ResumeRegistService {

	OcrReadCardImgVO readCardImg(OcrReadCardImgDTO ocrReadCardImgDTO);

	String saveCardInfo(OcrReadCardImgVO ocrReadCardImgVo);

	Integer addFaceImg(AddJobFaceDTO addJobFaceDTO);

	Result deliveryThird(long parseLong);

	List<DicContentVO> relationList();

	List<DicContentVO> getDegreeType();

	List<DicContentVO> getEducationType();

	Boolean bindMobile(VerifySmsCodeDTO verifySmsCodeAo);

	Boolean sendSmsCode(String mobile);


	Result saveEducationThird(ApplicationEducationDTO applicationEducationDTO);

	Result saveApplicationEmergencyThird(ApplicationEmergencyDTO emergencyDTO);

	Result saveThird(ApplicationFamilyDTO applicationFamilyDTO);

	Result addApplicationWorkThird(ApplicationWorkDTO applicationWorkDTO);

	Result updateThird(ApplicationFamilyDTO applicationFamilyDTO);

	Result updateApplicationWorkThird(ApplicationWorkDTO applicationWorkDTO);

	Result updateApplicationeEducation(ApplicationEducationDTO applicationEducationDTO);

	Result getByApplicationIdThird(String applicationId);

	List<DicContentVO> emergencyRelationList();

}
