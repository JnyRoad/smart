package com.tce.smart.platform.service.watermeter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.watermeter.EleMeterConcentratorAddDTO;
import com.tce.smart.platform.api.dto.req.watermeter.EleMeterConcentratorQueryDTO;
import com.tce.smart.platform.api.dto.req.watermeter.EleMeterConcentratorUpdateDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeterConcentrator;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:14
 */
public interface SmtEleMeterConcentratorService extends IService<SmtEleMeterConcentrator> {

	/**
	 * 新增电表集中器接口
	 *
	 * @param dto
	 * @return
	 */
	Boolean addConcentrator(EleMeterConcentratorAddDTO dto);

	/**
	 * 修改电表集中器接口
	 *
	 * @param dto
	 * @return
	 */
	Boolean updateConcentrator(EleMeterConcentratorUpdateDTO dto);

	/**
	 * 删除水表集中器
	 *
	 * @param id
	 * @return
	 */
	Boolean delConcentrator(Long id);

	/**
	 * 分页查询电表集中器
	 *
	 * @param page
	 * @param queryDto
	 * @return
	 */
	IPage<SmtEleMeterConcentrator> getPage(Page page, EleMeterConcentratorQueryDTO queryDto);

	/**
	 * 电表集中器查询档案
	 *
	 * @param id
	 * @return
	 */
	Boolean queryFile(Long id);

	/**
	 * 通过IP地址查询电表集中器
	 *
	 * @param ip
	 * @return
	 */
	SmtEleMeterConcentrator getByIp(String ip);
}
