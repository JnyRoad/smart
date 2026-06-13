package com.tce.smart.platform.service.leavecount;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementCountReqDTO;
import com.tce.smart.platform.api.dto.resp.leavecount.SettlementInfoDhrRespDTO;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementLog;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:02:04
 */
public interface SmtSettlementLogService extends IService<SmtSettlementLog> {

	/**
	 * 保存日志
	 * @param result
	 * @param smtSettlementInfo
	 * @return
	 */
	Boolean saveLog(Result<SettlementInfoDhrRespDTO> result, SettlementCountReqDTO smtSettlementInfo);

}
