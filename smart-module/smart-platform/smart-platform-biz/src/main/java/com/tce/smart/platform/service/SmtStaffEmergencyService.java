package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.dto.StaffEmergencyDTO;
import com.tce.smart.platform.core.entity.SmtStaffEmergency;


/**
 * 员工紧急联系人
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:30
 */
public interface SmtStaffEmergencyService extends IService<SmtStaffEmergency> {

	Result saveStaffEmergency(SmtStaffEmergency smtStaffEmergency);

	Result updateByIdStaffEmergency(StaffEmergencyDTO emergencyDTO);

	/**
	 * 根据工号获取
	 * @param employeeId
	 * @return
	 */
	Result getByStaffId(String employeeId);

}
