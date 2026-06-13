package com.tce.smart.platform.service.watermeter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.watermeter.*;
import com.tce.smart.platform.api.dto.resp.watermeter.SdMeterStatisticsRespDTO;
import com.tce.smart.platform.api.dto.resp.watermeter.SdUseStatisticsRespDTO;
import com.tce.smart.platform.core.entity.watermeter.SmtEleMeter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:37
 */
public interface SmtEleMeterService extends IService<SmtEleMeter> {

	/**
	 * 判断电表集中器是否关联电表
	 *
	 * @param conId
	 * @return
	 */
	Boolean existEleMeterByConcentratorId(Long conId);

	/**
	 * 批量读取电表读数
	 *
	 * @param operate
	 */
	void getReading(EleMeterOperateDTO operate);

	/**
	 * 批量重新下载档案
	 *
	 * @param operate
	 */
	void reDownload(EleMeterOperateDTO operate);
	/**
	 * 通过集中器id获取所有电表
	 *
	 * @param page
	 * @param dto
	 * @return
	 */
	IPage<SmtEleMeter> getPage(Page page, EleMeterQueryDTO dto);

	/**
	 * 添加电表
	 *
	 * @param dto
	 * @return
	 */
	Boolean addMeter(EleMeterAddDTO dto);

	/**
	 * 修改电表
	 *
	 * @param dto
	 * @param isChangeDevice 是否更换设备
	 * @return
	 */
	Boolean updateMeter(EleMeterUpdateDTO dto, Boolean isChangeDevice);

	/**
	 * 导入电表
	 *
	 * @param request
	 * @param response
	 * @param inputStream
	 * @return
	 */
	void excelImport(HttpServletRequest request, HttpServletResponse response, InputStream inputStream);

	/**
	 * 通过电表集中器ID和电表序号获取电表
	 *
	 * @param concentratorId
	 * @param seq
	 * @return
	 */
	SmtEleMeter getByConcentratorIdAndSeq(Long concentratorId, Integer seq);

	/**
	 * 通过电表集中器ID查询电表
	 *
	 * @param concentratorId
	 * @return
	 */
	List<SmtEleMeter> getByConcentratorId(Long concentratorId);

	/**
	 * 控制电表闸门
	 *
	 * @param eleMeterId
	 * @param status
	 * @return
	 */
	Boolean changeBrake(Long eleMeterId, Integer status);

	/**
	 * 批量控制电表闸门
	 *
	 * @param operate
	 * @return
	 */
	String batchChangeBrake(EleMeterBrakeOperateDTO operate);

	/**
	 * 修改阀门状态
	 *
	 * @param dto
	 * @return
	 */
	Boolean changeBrakeStatus(SmartBrakeUpdateDTO dto);

	/**
	 * 删除电表
	 *
	 * @param delList
	 * @return
	 */
	String remove(EleMeterOperateDTO delList);

	IPage<SdMeterStatisticsRespDTO> getMeterStatisticsPage(Page page, SdMeterStatisticsQueryDTO dto);

	List<SdMeterStatisticsRespDTO> getMeterStatisticsList(SdMeterStatisticsQueryDTO dto);

	IPage<SdUseStatisticsRespDTO> getUseStatisticsPage(Page page, SdUseStatisticsQueryDTO dto,Long[] deviceIds,Long[] deviceTagList);

	List<SdUseStatisticsRespDTO> getUseStatisticsList(SdUseStatisticsQueryDTO dto,Long[] deviceIds,Long[] deviceTagList);
}
