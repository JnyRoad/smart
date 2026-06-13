package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.dto.ApplicationEmergencyDTO;
import com.tce.smart.platform.core.entity.SmtApplicationEmergency;


/**
 * 员工紧急联系人
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:30
 */
public interface SmtApplicationEmergencyService extends IService<SmtApplicationEmergency> {

	Boolean saveApplicationEmergency(SmtApplicationEmergency smtApplicationEmergency);

	Integer updateByIdApplicationEmergency(ApplicationEmergencyDTO emergencyDTO);

	/**
	 * 根据工号获取
	 * @param employeeId
	 * @return
	 */
	Result getByApplicationId(String employeeId);



}
