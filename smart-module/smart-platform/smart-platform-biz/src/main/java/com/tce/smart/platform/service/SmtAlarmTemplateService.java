package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.AlarmTemplateDTO;
import com.tce.smart.platform.core.entity.SmtAlarmTemplate;

/**
 * 警报信息记录
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:58
 */
public interface SmtAlarmTemplateService extends IService<SmtAlarmTemplate> {

	/**
	 * 保持警报模板信息
	 * @param entity 警报模板信息
	 * @return
	 */
	boolean saveSmtAlarmRecever(AlarmTemplateDTO entity);

	/**
	 * 查询警报模板信息
	 * @return
	 */
	SmtAlarmTemplate getAlarmTemplate();


}
