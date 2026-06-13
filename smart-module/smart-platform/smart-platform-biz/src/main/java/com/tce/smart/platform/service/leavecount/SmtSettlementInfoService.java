package com.tce.smart.platform.service.leavecount;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementCountReqDTO;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementInfoQueryReqDTO;
import com.tce.smart.platform.api.dto.resp.leavecount.SettlementInfoDhrRespDTO;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementInfo;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:02:12
 */
public interface SmtSettlementInfoService extends IService<SmtSettlementInfo> {

	/**
	 * 分页数据
	 * @param page
	 * @param queryReqDTO
	 * @return
	 */
	IPage<SmtSettlementInfo> getPage(Page page, SettlementInfoQueryReqDTO queryReqDTO);

	/**
	 * 离职水电结算
	 * @return
	 */
	SettlementInfoDhrRespDTO getSettlement(SettlementCountReqDTO reqDTO);
}
