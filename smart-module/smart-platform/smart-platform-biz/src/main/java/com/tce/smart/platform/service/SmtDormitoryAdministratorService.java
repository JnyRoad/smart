package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.AddDormitoryAdministratorReqDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryAdministrator;

/**
 * @Title: SmtDormitoryAdministratorService
 * @Auther: guohongtai
 * @Date: 2020-10-14 15:29
 */

public interface SmtDormitoryAdministratorService extends IService<SmtDormitoryAdministrator> {
	SmtDormitoryAdministrator getByParkId(Integer parkId);

	Boolean saveDormitoryAdministrator(AddDormitoryAdministratorReqDTO reqDTO);
}
