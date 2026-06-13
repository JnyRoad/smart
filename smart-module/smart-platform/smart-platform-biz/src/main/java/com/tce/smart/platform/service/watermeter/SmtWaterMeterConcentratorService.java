package com.tce.smart.platform.service.watermeter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterConcentratorAddDTO;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterConcentratorQueryDTO;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterConcentratorUpdateDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeterConcentrator;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:30
 */
public interface SmtWaterMeterConcentratorService extends IService<SmtWaterMeterConcentrator> {

	/**
	 * 新增水表集中器接口
	 *
	 * @param dto
	 * @return
	 */
	Boolean addConcentrator(WaterMeterConcentratorAddDTO dto);

	/**
	 * 修改水表集中器接口
	 *
	 * @param dto
	 * @return
	 */
	Boolean updateConcentrator(WaterMeterConcentratorUpdateDTO dto);

	/**
	 * 删除水表集中器
	 *
	 * @param id
	 * @return
	 */
	Boolean delConcentrator(Long id);

	/**
	 * 分页查询水表集中器
	 *
	 * @param page
	 * @param queryDto
	 * @return
	 */
	IPage<SmtWaterMeterConcentrator> getPage(Page page, WaterMeterConcentratorQueryDTO queryDto);

	/**
	 * 水表集中器查询档案
	 *
	 * @param id
	 * @return
	 */
	Boolean queryFile(Long id);

	/**
	 * 通过集中器IP查询
	 *
	 * @param ip
	 * @return
	 */
	SmtWaterMeterConcentrator getByIp(String ip);
}
