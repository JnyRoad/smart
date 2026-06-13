package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtStaffFamily;

public interface SmtStaffFamilyService extends IService<SmtStaffFamily> {

	Result addStaffFamily(SmtStaffFamily smtStaffFamily);

	Result updateStaffFamily(SmtStaffFamily smtStaffFamily);

    Result getByStaffId(String employeeId);

	Result removeFamilyByStaffId(Long staffId);
}
