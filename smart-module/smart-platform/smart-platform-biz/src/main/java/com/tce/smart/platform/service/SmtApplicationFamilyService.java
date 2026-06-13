package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtApplicationFamily;

import java.util.List;

public interface SmtApplicationFamilyService extends IService<SmtApplicationFamily> {

	Result addApplicationFamily(SmtApplicationFamily smtApplicationFamily);

	Result updateApplicationFamily(SmtApplicationFamily smtApplicationFamily);

	List<SmtApplicationFamily> getByApplicationId(String employeeId);

	Result removeFamilyByApplicationId(Long applicationId);

}
