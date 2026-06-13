package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtStaffRelation;

/**
 * 员工人际关系表
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:26
 */
public interface SmtStaffRelationService extends IService<SmtStaffRelation> {

	Result addStaffRelation(SmtStaffRelation smtStaffRelation);

	Result updateStaffRelation(SmtStaffRelation smtStaffRelation);

    Result getByStaffId(String employeeId);

	Result removeRelationByStaffId(Integer staffId);
}
