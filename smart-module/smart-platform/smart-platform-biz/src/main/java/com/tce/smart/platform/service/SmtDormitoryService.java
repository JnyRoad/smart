package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.resp.DormitoryFloorRespDTO;
import com.tce.smart.platform.api.dto.resp.SmtDormitoryRespDTO;
import com.tce.smart.platform.core.entity.SmtDormitory;

import java.util.List;

/**
 * 园区宿舍楼表
 *
 * @author 齐佩
 * @date 2019-04-13 18:17:25
 */
public interface SmtDormitoryService extends IService<SmtDormitory> {

	List<DormitoryFloorRespDTO> getByParkId(Integer parkId, Boolean isAccount);

	/**
	 * 根据园区ID查询宿舍信息
	 * @param parkId
	 * @return
	 */
	List<SmtDormitoryRespDTO> getDormitoryByParkId(Integer parkId);

	Result addDormitory(SmtDormitory smtDormitory);

	Result updateDormitoryById(SmtDormitory smtDormitory);

	Result removeDormitoryById(Integer id);

	Result getSmtDormitoryPage(Page page, SmtDormitory smtDormitory);

	/**
	 * 通过园区id和名称获取楼栋
	 * @param parkId
	 * @param name
	 * @return
	 */
	SmtDormitory getByParkAndName(Integer parkId, String name);

}
