package com.tce.smart.platform.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.ao.SmtAppStaffAuthBatchSaveAO;
import com.tce.smart.platform.core.ao.SmtAppStaffAuthSaveAO;
import com.tce.smart.platform.core.entity.SmtAppStaffAuth;
import com.tce.smart.platform.core.vo.StaffAuthModuleHrVO;

/**
 * 员工App权限服务接口
 *
 * @author mckaywu
 * @date 2019-06-12 11:18:41
 */
public interface SmtAppStaffAuthService extends IService<SmtAppStaffAuth> {

	/**
	 * 获取员工权限列表
	 *
	 * @param staffId 员工ID
	 * @return 员工权限列表
	 */
	List<SmtAppStaffAuth> getStaffAuthList(Long staffId);

	/**
	 * 修改员工权限
	 *
	 * @param appStaffAuthSaveAO 员工权限表新增修改AO
	 * @return @return true-成功
	 */
	Boolean addStaffAuth(SmtAppStaffAuthSaveAO appStaffAuthSaveAO);

	/**
	 * 修改员工权限 先删除就数据，再添加新数据
	 *
	 * @param appStaffAuthSaveAO 员工权限表新增修改AO
	 * @return @return true-成功
	 */
	Boolean updateStaffAuth(SmtAppStaffAuthSaveAO appStaffAuthSaveAO);

	/**
	 * 批量修改员工权限 先删除就数据，再添加新数据
	 *
	 * @param appStaffAuthBatchaveAO 员工权限表批量新增修改AO
	 * @return @return true-成功
	 */
	Boolean batchUpdateStaffAuth(SmtAppStaffAuthBatchSaveAO appStaffAuthBatchaveAO);

	/**
	 * 删除员工权限
	 *
	 * @param staffId 员工ID
	 * @return true-成功
	 */
	Boolean deleteStaffAuth(Long staffId);

	/**
	 * 获取用户已分配的模块ID
	 *
	 * @param badge 员工工号
	 * @return
	 */
	List<String> getStaffModule(String badge);

	/**
	 * 获取员工可查看的简历岗位层级ID
	 *
	 * @param badge
	 * @return
	 */
	List<String> getStaffRecruitAuthLeve(String badge);

	/**
	 * 获取员工模块ID，hr权限ID
	 *
	 * @param badge 员工号
	 * @return 员工模块ID，hr权限ID(已去重复)
	 */
	StaffAuthModuleHrVO getAuthDetailIds(String badge);

	/**
	 * 初始化新入职员工App权限
	 * @param staffId
	 * @param parkId
	 * @return
	 */
	Boolean initStaffAuth(Long staffId, Integer parkId);

	/**
	 * 登陆初始化权限
	 * @param badge 员工号
	 * @return true-成功
	 */
	Boolean initLoginAuth(String badge);
}
