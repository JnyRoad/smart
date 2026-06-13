package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtApplicationEducation;
import com.tce.smart.platform.core.vo.EducationVO;

import java.util.List;

/**
 * 应聘者教育经验
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:38
 */
public interface SmtApplicationEducationService extends IService<SmtApplicationEducation> {

	Result saveEducation(SmtApplicationEducation smtApplicationEducation);

	Result updateApplicationeEducation(SmtApplicationEducation smtApplicationEducation);

	List<EducationVO> getSmtApplicationEducationList(String applicationId);

	Integer deleteEducationList(String applicationId);


}
