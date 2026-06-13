package com.tce.smart.platform.service.watermeter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.DeviceStateChangeDTO;
import com.tce.smart.platform.api.dto.req.watermeter.WaterValveConcentratorAddDTO;
import com.tce.smart.platform.api.dto.req.watermeter.WaterValveConcentratorQueryDTO;
import com.tce.smart.platform.api.dto.req.watermeter.WaterValveConcentratorUpdateDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterValveConcentrator;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:30
 */
public interface SmtWaterValveConcentratorService extends IService<SmtWaterValveConcentrator> {

	/**
	 * 新增外置阀门集中器接口
	 * @param dto
	 * @return
	 */
	Boolean addConcentrator(WaterValveConcentratorAddDTO dto);
	/**
	 * 修改外置阀门集中器接口
	 * @param dto
	 * @return
	 */
	Boolean updateConcentrator(WaterValveConcentratorUpdateDTO dto);

	/**
	 * 删除外置阀门集中器
	 * @param id
	 * @return
	 */
	Boolean delConcentrator(Long id);

	/**
	 * 分页查询外置阀门集中器
	 * @param page
	 * @param queryDto
	 * @return
	 */
	IPage<SmtWaterValveConcentrator> getPage(Page page, WaterValveConcentratorQueryDTO queryDto);
}
