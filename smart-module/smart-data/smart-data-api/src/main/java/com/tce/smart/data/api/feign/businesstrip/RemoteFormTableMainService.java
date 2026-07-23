package com.tce.smart.data.api.feign.businesstrip;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.businesstrip.resp.CcdFormtableMainDt1RespDTO;
import com.tce.smart.data.api.dto.businesstrip.resp.CcdFormtableMainDt2RespDTO;
import com.tce.smart.data.api.dto.businesstrip.resp.CcdFormtableMainRespDTO;
import com.tce.smart.data.api.dto.businesstrip.resp.VwHRMResourceRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


/**
 * 查询出差数据
 * @author liangyuan
 *
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteFormTableMainService {

	/**
	 * 查询出差列表
	 * @param current
	 * @param size
	 * @param pedestrianBadge
	 * @param from
	 * @return
	 */
	@GetMapping("/formtableMain/info")
	Result<Page<CcdFormtableMainRespDTO>> info(@RequestParam("current") final long current, @RequestParam("size") final long size, @RequestParam(
			"pedestrianBadge") final String pedestrianBadge, @RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);



	/**
	 * 查询出差详情
	 * @param mainId
	 * @param from
	 * @return
	 */
	@GetMapping("/formtableMain/infoTravel")
	Result<CcdFormtableMainRespDTO> infoTravel(@RequestParam("mainId") final Integer mainId,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);
	/**
	 * 查询出差日程
	 * @param mainId
	 * @param from
	 * @return
	 */
	@GetMapping("/formtableMain/infoDay")
	Result<List<CcdFormtableMainDt1RespDTO>> infoDay(@RequestParam("mainId") final Integer mainId,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	/**
	 * 查询出差报告
	 * @param mainId
	 * @param from
	 * @return
	 */
	@GetMapping("/formtableMain/infoReport")
	Result<List<CcdFormtableMainDt2RespDTO>> infoReport(@RequestParam("mainId") final Integer mainId,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	/**
	 * 查询员工
	 * @param id
	 * @param from
	 * @return
	 */
	@GetMapping("/formtable/infoPerson")
	Result<VwHRMResourceRespDTO> infoPerson(@RequestParam("id") final Integer id,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);
}
