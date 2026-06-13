package com.tce.smart.data.api.feign.ehrview;


import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsCallOwanceCancelAllRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * <p>
 *  撤销审批外宿记录接口
 * </p>
 *
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteOvwYsCallOwanceCancelService {

	/**
	 * 查询员工外宿审批撤销记录
	 * @param badge 员工号
	 * @param xtype 补贴类型
	 * @param begindate 补贴开始时间
	 * @return
	 */
    @GetMapping("/ovwYsCallOwanceCancel/get")
    Result<List<OvwYsCallOwanceCancelAllRespDTO>> getInfo(@RequestParam("badge") String badge,
														  @RequestParam("xtype") Integer xtype,
														  @RequestParam("begindate") String begindate);

}
