package com.tce.smart.app.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.app.api.dto.AddIdCollectDto;
import com.tce.smart.app.dto.fore.OcrIdCardDto;
import com.tce.smart.app.entity.AppIdentityCollect;

/**
 * 身份证信息采集信息
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:44:43
 */
public interface AppIdentityCollectService extends IService<AppIdentityCollect> {

	/**
	 * 根据员工号查询
	 *
	 * @param badge 员工号
	 * @return
	 */
	AppIdentityCollect getInfoByBadge(String badge);

	/**
	 * 根据员工号查询身份证采集信息
	 *
	 * @param staffId 员工号
	 * @return List<AppIdentityCollect> 身份证采集信息
	 */
	List<AppIdentityCollect> getByStaffId(String staffId);

	/**
	 * 根据身份证号查询身份证采集信息
	 *
	 * @param identity 身份证号
	 * @return List<AppIdentityCollect> 身份证采集信息
	 */
	List<AppIdentityCollect> getByIdentity(String identity);

	/**
	 * 更新员工头像同步状态
	 *
	 * @param perfectId 表id
	 * @param syncState 同步状态
	 * @return
	 */
	boolean updatePhtoSync(Integer perfectId, String syncState);

	/**
	 * 添加员工信息手机-人脸
	 *
	 * @param addIdCollectDto 人脸信息
	 * @return true-成功，fasle-失败
	 */
	boolean saveFaceCollect(AddIdCollectDto addIdCollectDto);

	/**
	 * 插入或者更新员工收集信息
	 *
	 * @param ocrIdCardDto 信息完善Dto
	 * @return 等于1：失败，不等于-1:新增数据主键ID
	 */
	int insertOrUpdate(OcrIdCardDto ocrIdCardDto);

	/**
	 * 分页查询未同步的员工人脸照片信息
	 *
	 * @param page 分页对象
	 * @return 分页数据
	 */
	IPage<AppIdentityCollect> getLatestPhoto(Page<AppIdentityCollect> page);
}
