package com.tce.smart.app.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.app.entity.AppAdverInfo;

import java.util.List;

/***
 * description: app广告管理服务接口 <br>
 * date: 2019/12/30 17:51 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
public interface AppAdverInfoService extends IService<AppAdverInfo> {

	/**
	 * 分页展示
	 *
	 * @param page         分页信息
	 * @param appAdverInfo 查询条件
	 * @return
	 */
	IPage<AppAdverInfo> listByPage(Page page, AppAdverInfo appAdverInfo);

	/**
	 * 新增
	 *
	 * @param saveAo 新增 信息
	 * @return 成功-true,失败-false
	 */
	Boolean saveAdver(AppAdverInfo saveAo);

	/**
	 * 根据id更新
	 *
	 * @param saveAo 更新信息
	 * @return 成功-true,失败-false
	 */
	Boolean updateAdverById(AppAdverInfo saveAo);

	/**
	 * 取消发布广告
	 *
	 * @param id 修改ID
	 * @return 成功-true,失败-false
	 */
	Boolean publish(Integer id);

	/**
	 * 发布广告
	 *
	 * @param id 修改ID
	 * @return 成功-true,失败-false
	 */
	Boolean unpPublish(Integer id);

	/**
	 * 逻辑删除广告
	 *
	 * @param id id 修改ID
	 * @return 成功-true,失败-false
	 */
	Boolean deleteAdver(Integer id);

	/**
	 * 根据广告位置获取广告列表
	 * @param adverPosition 广告位置
	 * @return 广告列表
	 */
	List<AppAdverInfo> getAdverByPosition(Integer adverPosition);
}
