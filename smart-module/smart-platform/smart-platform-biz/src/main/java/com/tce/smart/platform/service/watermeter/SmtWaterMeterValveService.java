package com.tce.smart.platform.service.watermeter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.watermeter.SmartValveDataUpdateDTO;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterValveAddDTO;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterValveQueryDTO;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterValveUpdateDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeterValve;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:32
 */
public interface SmtWaterMeterValveService extends IService<SmtWaterMeterValve> {

	/**
	 * 新增水表阀门
	 * @param dto
	 * @return
	 */
	Boolean saveValve(WaterMeterValveAddDTO dto);
	/**
	 * 修改水表阀门
	 * @param dto
	 * @return
	 */
	Boolean updateValve(WaterMeterValveUpdateDTO dto);

	/**
	 * 通过阀门id，修改阀门开启关闭状态
	 * @param valveId
	 * @param status
	 * @return
	 */
	Boolean changeValveStatus(Long valveId, Integer status);

	/**
	 * 通过阀门id，修改阀门远程功能开启关闭状态
	 * @param valveId
	 * @param status
	 * @return
	 */
	Boolean changeValveRemoteStatus(Long valveId, Integer status);

	/**
	 * 修改阀门状态
	 * @param dto
	 * @return
	 */
	Boolean changeValveStatus(SmartValveDataUpdateDTO dto);
	/**
	 * 分页获取水表阀门
	 * @param page
	 * @param dto
	 * @return
	 */
	IPage<SmtWaterMeterValve> getPage(Page page, WaterMeterValveQueryDTO dto);

	/**
	 * 判断集中器是否已关联水表
	 *
	 * @param conId
	 * @return
	 */
	Boolean existWaterMeterByConcentratorId(Long conId);
}
