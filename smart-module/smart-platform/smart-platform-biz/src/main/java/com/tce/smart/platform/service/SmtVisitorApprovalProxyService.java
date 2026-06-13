package com.tce.smart.platform.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.visitormanage.VisitorProxyQueryReqDTO;
import com.tce.smart.platform.api.dto.req.visitormanage.VisitorProxyReqDTO;
import com.tce.smart.platform.api.dto.req.visitormanage.VisitorWhiteQueryReqDTO;
import com.tce.smart.platform.api.dto.req.visitormanage.VisitorWhiteReqDTO;
import com.tce.smart.platform.api.dto.resp.visitormanage.VisitorProxyQueryRespDTO;
import com.tce.smart.platform.api.dto.resp.visitormanage.VisitorWhiteQueryRespDTO;
import com.tce.smart.platform.core.entity.SmtVisitorApprovalProxy;
import com.tce.smart.platform.core.entity.SmtVisitorApprovalWhite;

import java.util.List;

/**
 * 访客审批代理
 *
 */
public interface SmtVisitorApprovalProxyService extends IService<SmtVisitorApprovalProxy> {

	/**
	 * 分页查询
	 * @param visitorProxyQueryReqDTO
	 * @return
	 */
	IPage<VisitorProxyQueryRespDTO> pageQuery(VisitorProxyQueryReqDTO visitorProxyQueryReqDTO);

	/**
	 * 添加代理
	 * @param visitorProxyReqDTO
	 * @return
	 */
	Boolean saveProxy(VisitorProxyReqDTO visitorProxyReqDTO);

	/**
	 * 根据ID批量删除
	 * @param ids
	 * @return
	 */
	Boolean batchDel(List<Long> ids);
}
