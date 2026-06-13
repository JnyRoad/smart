package com.tce.smart.platform.service.settlement;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.dailySd.DailyMeterQueryDTO;
import com.tce.smart.platform.api.dto.resp.dailySd.DailyMeterRespDTO;
import com.tce.smart.platform.core.entity.SmtSdMeterreadDetailDaily;

import java.util.List;

/**
 * @description: SmtSdMeterreadService
 * @date: 2020-07-10 9:45
 * @author: wuling
 * @version: 1.0
 */
public interface SmtSdMeterreadDetailDailyService extends IService<SmtSdMeterreadDetailDaily> {


	/**
	 * 按楼层查询所有房间的抄表信息
	 * @param queryDTO
	 * @return
	 */
	List<DailyMeterRespDTO> getFloorSdMeterReadNew(DailyMeterQueryDTO queryDTO);

	/**
	 * 每日水电结算
	 */
	void genDailyRecord();

}
