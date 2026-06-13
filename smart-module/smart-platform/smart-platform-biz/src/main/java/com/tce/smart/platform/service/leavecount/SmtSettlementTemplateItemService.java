package com.tce.smart.platform.service.leavecount;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementTemplateEditReqDTO;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementTemplateItem;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:01:40
 */
public interface SmtSettlementTemplateItemService extends IService<SmtSettlementTemplateItem> {

	/**
	 * 编辑项
	 * @param reqDTO
	 * @return
	 */
	Boolean editItem(SettlementTemplateEditReqDTO reqDTO);

	/**
	 * 删除项
	 * @param itemId
	 * @return
	 */
	Boolean removeItem(Long itemId);
}
