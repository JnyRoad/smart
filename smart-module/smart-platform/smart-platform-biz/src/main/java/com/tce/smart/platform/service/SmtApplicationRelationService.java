package com.tce.smart.platform.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtApplicationRelation;
import com.tce.smart.platform.core.vo.OrgrelationVO;

/**
 * 应聘者人际关系表
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:26
 */
public interface SmtApplicationRelationService extends IService<SmtApplicationRelation> {

	Result addApplicationRelation(SmtApplicationRelation smtApplicationRelation);

	Result updateApplicationRelation(SmtApplicationRelation smtApplicationRelation);

    Result getByApplicationId(String employeeId);

	Result removeRelationByApplicationId(Long applicationId);

	List<OrgrelationVO> getApplicationInfo(String applicationId);
}
