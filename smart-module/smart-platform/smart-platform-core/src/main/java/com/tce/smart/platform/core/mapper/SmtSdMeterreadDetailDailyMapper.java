package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.api.dto.resp.dailySd.DailyMeterRespDTO;
import com.tce.smart.platform.core.entity.SmtSdMeterreadDetailDaily;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description: SmtSdMeterreadMapper
 * @date: 2020-07-10
 * @author: wuling
 * @version: 1.0
 */
public interface SmtSdMeterreadDetailDailyMapper extends BaseMapper<SmtSdMeterreadDetailDaily> {

	/**
	 * 水电日计算读取
	 *
	 * @param dormitoryId
	 * @param floorId
	 * @param roomId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	List<DailyMeterRespDTO> getFloorSdMeterReadNew(@Param("dormitoryId") Integer dormitoryId, @Param("floorId") Integer floorId,
												   @Param("roomId") Integer roomId, @Param("startTime") String startTime,
												   @Param("endTime") String endTime);
}
