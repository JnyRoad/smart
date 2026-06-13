package com.tce.smart.platform.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.visitormanage.VisitorWhiteQueryReqDTO;
import com.tce.smart.platform.api.dto.req.visitormanage.VisitorWhiteReqDTO;
import com.tce.smart.platform.api.dto.resp.visitormanage.VisitorWhiteQueryRespDTO;
import com.tce.smart.platform.core.entity.SmtVisitorApprovalWhite;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 访客审批白名单
 *
 */
public interface SmtVisitorApprovalWhiteService extends IService<SmtVisitorApprovalWhite> {

	/**
	 * 分页查询
	 * @param visitorWhiteReqDTO
	 * @return
	 */
	IPage<VisitorWhiteQueryRespDTO> pageQuery(VisitorWhiteQueryReqDTO visitorWhiteReqDTO);

	/**
	 * 添加白名单
	 * @param visitorWhiteReqDTO
	 * @return
	 */
	Boolean saveItem(VisitorWhiteReqDTO visitorWhiteReqDTO);

	/**
	 * 根据ID批量删除
	 * @param ids
	 * @return
	 */
	Boolean batchDel(List<Long> ids);
}
