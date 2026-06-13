package com.tce.smart.platform.service.settlement;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.ResetSdDetailReqDTO;
import com.tce.smart.platform.api.dto.req.sddto.SdMeterreadDetailReqDTO;
import com.tce.smart.platform.api.dto.resp.SmtSdMeterreadDetailRespDTO;
import com.tce.smart.platform.core.entity.SmtSdMeterreadDetail;

import java.util.Date;

/**
 * @description: SmtSdMeterreadDetailService
 * @date: 2020-07-13 15:49
 * @author: wuling
 * @version: 1.0
 */
public interface SmtSdMeterreadDetailService extends IService<SmtSdMeterreadDetail> {

	/**
	 * 添加抄表详细
	 * 这里是两个动作
	 * 	1. 如数据不操作 则添加
	 * 	2. 如果数据存在则修改
	 * @param sdMeterreadDetailReqDTO
	 * @return
	 */
	Boolean saveMeterReadDetail(SdMeterreadDetailReqDTO sdMeterreadDetailReqDTO,SmtSdMeterreadService smtSdMeterreadService);

	/**
	 * 获取抄表详细记录
	 * @param mrId
	 * @return
	 */
	SmtSdMeterreadDetailRespDTO getMeterReadDetail(Long mrId);

	/**
	 * 查询上月的抄表数据 如果上月未结算 则不返回数据
	 * @param roomId
	 * @param meterMonth
	 * @return
	 */
	SmtSdMeterreadDetailRespDTO getPreMonthDetail(Integer roomId, Date meterMonth);

	/**
	 * 重置水电
	 * @param resetSdDetailReqDTO
	 * @return
	 */
	Boolean resetSdMeterDetail(ResetSdDetailReqDTO resetSdDetailReqDTO,SmtSdMeterreadService smtSdMeterreadService);

}
