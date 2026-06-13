package com.tce.smart.platform.service.watermeter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterChangeQueryDTO;
import com.tce.smart.platform.api.dto.req.watermeter.WaterMeterUpdateDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeterChange;

/**
 * @author Li.JiaJun
 * @since 2022/5/12 10:53
 */
public interface SmtWaterMeterChangeService extends IService<SmtWaterMeterChange> {

	/**
	 * 分页查询
	 *
	 * @param page
	 * @param dto
	 * @return
	 */
	IPage<SmtWaterMeterChange> getPage(Page page, WaterMeterChangeQueryDTO dto);

	/**
	 * 新增水表更换记录
	 *
	 * @param concentratorId
	 * @param meterId
	 * @param beforeAddress
	 * @param beforePort
	 * @param largeClass
	 * @param seq
	 * @param dto
	 */
	void addRecord(Long concentratorId, Long meterId, String beforeAddress, String beforePort,
				   String largeClass, Integer seq, WaterMeterUpdateDTO dto);
}
