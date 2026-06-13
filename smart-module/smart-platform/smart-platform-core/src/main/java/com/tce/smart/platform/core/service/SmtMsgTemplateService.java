package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.dto.MsgTemplateDTO;
import com.tce.smart.platform.core.entity.SmtMsgTemplate;

import java.util.List;

/**
 * 消息模板服务接口
 *
 * @author mingkai.wu
 * @date 2019-05-15 18:15:32
 */
public interface SmtMsgTemplateService extends IService<SmtMsgTemplate> {

	Result getSmtMessageTemplate();

	Result getSmtEmainTemplateService();

	Result getByCode(String code);

	Result update(MsgTemplateDTO msgTemplateDTO);

	Result getEmailById(Integer id);

	Result updateReceive(MsgTemplateDTO msgTemplateDTO);

	/**
	 * 根据模板编码查询模板信息
	 *
	 * @param tempCode 模板编码
	 * @return SmtMsgTemplate 模板信息
	 */
	SmtMsgTemplate selectByTempCode(String tempCode);

	/**
	 * 获得短信模板
	 *
	 * @return
	 */
	List<SmtMsgTemplate> getMsgTemplate();
}
