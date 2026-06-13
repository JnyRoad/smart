package com.tce.smart.platform.service.leavecount;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementTemplateReqDTO;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementTemplate;

/**
 * @author fushiping
 * @date 2022-06-21 11:01:56
 */
public interface SmtSettlementTemplateService extends IService<SmtSettlementTemplate> {

	/**
	 * 新增编辑模板
	 *
	 * @param reqDTO
	 * @return
	 */
	Boolean editTemp(SettlementTemplateReqDTO reqDTO);

	/**
	 * 通过房间id查询模板
	 *
	 * @param roomId
	 * @return
	 */
	SmtSettlementTemplate getByRoomId(Integer roomId);
}
