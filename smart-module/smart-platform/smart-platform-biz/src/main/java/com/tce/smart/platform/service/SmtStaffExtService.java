package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.TempStaffEditReqDTO;
import com.tce.smart.platform.core.dto.ApplicationStaffDTO;
import com.tce.smart.platform.core.entity.SmtOrganizeRelation;
import com.tce.smart.platform.core.entity.SmtPreStaff;
import com.tce.smart.platform.core.entity.SmtStaff;

import java.util.List;

/**
 * @author sunfujian
 * @since 2021/9/8 9:18
 */
public interface SmtStaffExtService extends IService<SmtStaff> {

	Result getAuthInfo(String id);

	/**
	 * 员工号自动生成
	 * @param compId
	 * @return
	 */
	String createNewBadge(String compId);

	void addStaffRelation(SmtPreStaff preStaff, Long applicationId);

	void addStaffFamily(SmtPreStaff preStaff, Long applicationId);

	/**
	 * 同步用户信息到HR
	 * @param preStaff
	 * @param applicationId
	 * @param local
	 */
	void addStaffToHR(SmtPreStaff preStaff, Long applicationId, Integer local);

	Result addStaffToHR(ApplicationStaffDTO smtStaffReq);
	/**
	 * 添加员工共经验
	 *
	 * @param applicationId
	 * @param
	 */
	void addStaffWork(Long applicationId, SmtPreStaff preStaff);
	/**
	 * 添加员工教育
	 *
	 * @param applicationId
	 * @param
	 */
	void addStaffEducation(Long applicationId, SmtPreStaff preStaff);

	Boolean saveBatchTemporaryStaff(List<TempStaffEditReqDTO> tempStaffs, SmtDormitoryStaffService smtDormitoryStaffService);

	Boolean saveBatchTemporaryStaff(List<TempStaffEditReqDTO> tempStaffs, SmtOrganizeRelation organizeRelation, SmtDormitoryStaffService smtDormitoryStaffService);

	Boolean authAccess(SmtStaff staff);
}
