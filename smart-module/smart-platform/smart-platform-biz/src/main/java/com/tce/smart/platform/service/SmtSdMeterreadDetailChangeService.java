package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.resp.commonsd.SdMeterreadDetailChangeDTO;
import com.tce.smart.platform.core.entity.SmtSdMeterreadDetailChange;

import java.util.Date;
import java.util.List;

/**
 * @author Li.JiaJun
 * @since 2022/7/21 17:29
 */
public interface SmtSdMeterreadDetailChangeService extends IService<SmtSdMeterreadDetailChange> {

	/**
	 * 获取该月该房间换表详情
	 *
	 * @param meterMonth
	 * @param roomId
	 * @return
	 */
	List<SdMeterreadDetailChangeDTO> getList(Date meterMonth, Integer roomId);
}
