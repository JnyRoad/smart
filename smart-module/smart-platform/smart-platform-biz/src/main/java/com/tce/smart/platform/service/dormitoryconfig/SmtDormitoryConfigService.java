package com.tce.smart.platform.service.dormitoryconfig;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.dormitoryconfig.DormitoryConfigEditReqDTO;
import com.tce.smart.platform.core.entity.dormitoryconfig.SmtDormitoryConfig;

import java.util.List;

/**
 *
 * @author fushiping
 * @date 2021-09-14 20:14:53
 */
public interface SmtDormitoryConfigService extends IService<SmtDormitoryConfig> {

	/**
	 * 编辑配置
	 * @param editReqDTO
	 * @return
	 */
	Boolean editConfig(DormitoryConfigEditReqDTO editReqDTO);

	/**
	 * 分页获得配置列表
	 * @param page
	 * @return
	 */
	IPage<SmtDormitoryConfig> getPage(Page page, Integer parkId);

	/**
	 * 获得配置项
	 * @param parkId
	 * @return
	 */
	SmtDormitoryConfig getByParkId(Integer parkId);

	/**
	 * 获得关联BU
	 * @param parkId
	 * @return
	 */
	List<String> getRelationBu(Integer parkId);

	/**
	 * 获得关联BU
	 * @param parkId
	 * @return
	 */
	List<String> getRelationBus(List<Integer> parkId);
}
