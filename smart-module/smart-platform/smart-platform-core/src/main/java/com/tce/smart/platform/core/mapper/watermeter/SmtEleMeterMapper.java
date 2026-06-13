package com.tce.smart.platform.core.mapper.watermeter;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.api.dto.req.watermeter.SdMeterStatisticsQueryDTO;
import com.tce.smart.platform.api.dto.req.watermeter.SdUseStatisticsQueryDTO;
import com.tce.smart.platform.api.dto.resp.watermeter.SdUseStatisticsRespDTO;
import com.tce.smart.platform.core.dto.meter.SdDeviceRecordDTO;
import com.tce.smart.platform.core.dto.meter.SdMonthStatisticsDTO;
import com.tce.smart.platform.core.dto.meter.SdUseStatisticsDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeter;
import com.tce.smart.platform.core.vo.SmtEleMeterVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:12
 */
public interface SmtEleMeterMapper extends BaseMapper<SmtEleMeter> {

	IPage<SmtEleMeterVO> getPage(Page page);

	IPage<SdDeviceRecordDTO> getSdMeterStatisticsPage(Page page, @Param("param") SdMeterStatisticsQueryDTO dto);

	IPage<SdDeviceRecordDTO> getSdUseStatisticsPage(Page page, @Param("param") SdUseStatisticsQueryDTO dto,@Param("meterIds")List<Long> meterIds);

	List<SdDeviceRecordDTO> getSdUseStatisticsList(@Param("param") SdUseStatisticsQueryDTO dto,@Param("meterIds")List<Long> meterIds);

	IPage<SdDeviceRecordDTO> getWaterMeterStatisticsPage(Page page, @Param("param") SdMeterStatisticsQueryDTO dto);

	IPage<SdDeviceRecordDTO> getWaterUseStatisticsPage(Page page, @Param("param") SdUseStatisticsQueryDTO dto,@Param("meterIds")List<Long> meterIds);

	List<SdDeviceRecordDTO> getWaterUseStatisticsList(@Param("param") SdUseStatisticsQueryDTO dto,@Param("meterIds")List<Long> meterIds);

	IPage<SdDeviceRecordDTO> getEleMeterStatisticsPage(Page page, @Param("param") SdMeterStatisticsQueryDTO dto);

	IPage<SdDeviceRecordDTO> getEleUseStatisticsPage(Page page, @Param("param") SdUseStatisticsQueryDTO dto, @Param("meterIds")List<Long> meterIds);

	List<SdDeviceRecordDTO> getEleUseStatisticsList(@Param("param") SdUseStatisticsQueryDTO dto, @Param("meterIds")List<Long> meterIds);


	List<SdDeviceRecordDTO> getSdMeterStatisticsList(@Param("param") SdMeterStatisticsQueryDTO dto);

	List<SdDeviceRecordDTO> getWaterMeterStatisticsList(@Param("param") SdMeterStatisticsQueryDTO dto);

	List<SdDeviceRecordDTO> getEleMeterStatisticsList(@Param("param") SdMeterStatisticsQueryDTO dto);

	List<SdMonthStatisticsDTO> getWaterMonthUse(@Param("meterIds")List<Long> meterIds,@Param("startTime")String startTime,@Param("endTime")String endTime);

	List<SdMonthStatisticsDTO> getEleMonthUse(@Param("meterIds")List<Long> meterIds,@Param("startTime")String startTime,@Param("endTime")String endTime);

	List<SdUseStatisticsDTO> getEleMonthUse2(@Param("meterIds")List<Long> meterIds, @Param("startTime")String startTime, @Param("endTime")String endTime);

	List<Long> getMeterIdWithTag(@Param("tagName")String tagName);

	List<SdUseStatisticsRespDTO> getEleHistoryStatistics(@Param("meterIds")List<Long> meterIds, @Param("startTime")String startTime, @Param("endTime")String endTime);

	List<SdUseStatisticsRespDTO> getWaterHistoryStatistics(@Param("meterIds")List<Long> meterIds, @Param("startTime")String startTime, @Param("endTime")String endTime);
}
