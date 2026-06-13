package com.tce.smart.platform.service;




import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.UpdateDeviceAuthDTO;
import com.tce.smart.platform.core.entity.*;

import java.util.List;

public interface SmtStaffDeviceAuthService extends IService<SmtStaffDeviceAuth> {

	Integer removeAuthById(Integer id);

	Boolean removeByAuthId(Integer authId);


	Integer addAuth(SmtStaffDeviceAuth auth);


	Boolean updateAuth(UpdateDeviceAuthDTO auth);

	Boolean updateAuth(List<Long> staffIds, List<Integer> deviceAuthIds);

	/**
	 * 按差量调整员工权限：只新增 addedAuthIds、删除 removedAuthIds，员工已有的其他权限保持不动
	 */
	Boolean applyAuthDiff(List<Long> staffIds, List<Integer> addedAuthIds, List<Integer> removedAuthIds);

	List<SmtStaffDeviceAuth> queryList(Long staffId);

	List<SmtStaffDeviceAuth> querySecurityAuth(Integer parkId);

	Boolean removeAuthToDevice(List<Integer> ids, List<String> deviceIds);

	Boolean removeAuthToDevice(SmtStaffDeviceAuth staffAuth, List<String> deviceIds);

	String updateAuthNew(Integer type, UpdateDeviceAuthDTO auth);

	/**
	 * 下发进度结果
	 * @param auth
	 * @return
	 */
	List<SmtDeviceTaskDetail> getTaskResult(String auth);
}
