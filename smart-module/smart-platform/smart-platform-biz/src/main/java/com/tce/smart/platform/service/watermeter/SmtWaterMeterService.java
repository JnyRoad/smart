package com.tce.smart.platform.service.watermeter;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.watermeter.*;
import com.tce.smart.platform.core.entity.watermeter.SmtWaterMeter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:36
 */
public interface SmtWaterMeterService extends IService<SmtWaterMeter> {

	/**
	 * 判断集中器是否已关联水表
	 *
	 * @param conId
	 * @return
	 */
	Boolean existWaterMeterByConcentratorId(Long conId);

	/**
	 * 批量读取水表读数
	 *
	 * @param operate
	 */
	void getReading(WaterMeterOperateDTO operate);

	/**
	 * 批量重新下载水表档案
	 *
	 * @param operate
	 */
	void reDownload(WaterMeterOperateDTO operate);
	/**
	 * 通过集中器id获取所有水表
	 *
	 * @param page
	 * @param dto
	 * @return
	 */
	IPage<SmtWaterMeter> getPage(Page page, WaterMeterQueryDTO dto);

	/**
	 * 添加水表
	 *
	 * @param dto
	 * @return
	 */
	Boolean addMeter(WaterMeterAddDTO dto);

	/**
	 * 修改水表
	 *
	 * @param dto
	 * @param isChangeDevice 是否更换设备
	 * @return
	 */
	Boolean updateMeter(WaterMeterUpdateDTO dto, Boolean isChangeDevice);

	/**
	 * 导入水表
	 *
	 * @param request
	 * @param response
	 * @param inputStream
	 * @return
	 */
	void excelImport(HttpServletRequest request, HttpServletResponse response, InputStream inputStream);

	/**
	 * 控制阀门开关
	 *
	 * @param id
	 * @param status
	 * @return
	 */
	Boolean changeValveStatus(Long id, Integer status);

	/**
	 * 批量控制阀门开关
	 *
	 * @param operate
	 * @return
	 */
	String batchChangeValveStatus(WaterMeterValveOperateDTO operate);

	/**
	 * 修改阀门状态
	 *
	 * @param dto
	 * @return
	 */
	Boolean changeValveStatus(SmartValveDataUpdateDTO dto);

	/**
	 * 通过水表集中器ID和水表序号获取水表
	 *
	 * @param concentratorId
	 * @param seq
	 * @return
	 */
	SmtWaterMeter getByConcentratorIdAndSeq(Long concentratorId, Integer seq);

	/**
	 * 通过水表集中器ID获取水表
	 *
	 * @param concentratorId
	 * @return
	 */
	List<SmtWaterMeter> getByConcentratorId(Long concentratorId);

	/**
	 * 删除水表
	 *
	 * @param delList
	 * @return
	 */
	String remove(WaterMeterOperateDTO delList);
}
