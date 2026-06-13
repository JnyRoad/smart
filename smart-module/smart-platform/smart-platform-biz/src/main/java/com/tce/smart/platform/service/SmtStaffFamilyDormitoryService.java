package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.SmtTemplatesRuleReqDTO;
import com.tce.smart.platform.api.dto.req.StaffFamilyDormitoryReqDTO;
import com.tce.smart.platform.core.entity.SmtStaffFamilyDormitory;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @description: SmtStaffFamilyDormitoryService
 * @date: 2020-12-08 17:26
 * @author: wuling
 * @version: 1.0
 */
public interface SmtStaffFamilyDormitoryService extends IService<SmtStaffFamilyDormitory> {

	/**
	 * 添加员工家属
	 * @param staffFamilyDormitoryReqDTO
	 * @return
	 */
	Boolean addFamily(StaffFamilyDormitoryReqDTO staffFamilyDormitoryReqDTO);

	/**
	 * 删除家属入住
	 * @param id
	 * @return
	 */
	Boolean delFamily(Long id);

	/**
	 * 查询家属
	 * @param staffBadge
	 * @return
	 */
	List<StaffFamilyDormitoryReqDTO> queryFamily(String staffBadge);
}
