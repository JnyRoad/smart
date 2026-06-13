package com.tce.smart.admin.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.admin.api.entity.SysUserPark;

import java.util.List;

/***
 * description: 用户园区表 服务接口 <br>
 * date: 2019/11/20 17:27 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
public interface SysUserParkService extends IService<SysUserPark> {

	/**
	 * 根据用户Id查询该用户的关联的园区
	 * @param userId
	 * @return List<SysUserPark>
	 */
	List<SysUserPark> getListByUserId(Integer userId);

	/**
	 * 根据用户Id删除该用户的园区关系
	 * @param userId 用户ID
	 * @return boolean
	 */
	boolean deleteByUserId(Integer userId);


	/**
	 * 批量修改用户园区
	 * @param userId 用户id
	 * @param parkIdList 园区id集合
	 * @return boolea ture-成功，false-失败
	 */
	boolean saveUserParkBatch(Integer userId,List<Integer> parkIdList);

	/**
	 * 修改用户园区
	 * @param userId 用户id
	 * @param parkIdList 园区id集合
	 * @return boolean ture-成功，false-失败
	 */
	boolean updateUserPark(Integer userId,List<Integer> parkIdList);

	/**
	 * 查询用户园区列表
	 * @param userId 用户id
	 * @return List<SysUserPark> 用户园区列表
	 */
	List<SysUserPark> getUserParkList(Integer userId);
}
