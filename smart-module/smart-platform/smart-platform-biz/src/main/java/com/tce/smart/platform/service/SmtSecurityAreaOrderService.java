package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.securityarea.SmtSecurityAreaOrderReqDTO;
import com.tce.smart.platform.api.dto.resp.securityarea.SecurityAreaOrderDetailDTO;
import com.tce.smart.platform.api.dto.resp.securityarea.SecurityAreaOrderListDTO;
import com.tce.smart.platform.core.dto.securityarea.SecurityAreaOrderDTO;
import com.tce.smart.platform.core.entity.securityarea.SmtSecurityAreaOrder;

/**
 * @description: SmtSecurityAreaOrderService
 * @date: 2020-07-30 9:11
 * @author: wuling
 * @version: 1.0
 */
public interface SmtSecurityAreaOrderService extends IService<SmtSecurityAreaOrder> {

	/**
	 * 添加保密区预约
	 * @param smtSecurityAreaOrderReqDTO
	 * @return
	 */
	boolean saveOrder(SmtSecurityAreaOrderReqDTO smtSecurityAreaOrderReqDTO);

	/**
	 * 分页获取当前登录用户的保密区预约记录
	 * @return
	 */
	IPage<SecurityAreaOrderListDTO> getOrderListByUser(Page page);

	/**
	 * 获取保密区预约详情
	 * @param id
	 * @return
	 */
	SecurityAreaOrderDetailDTO getOrderDetail(Long id);
}
