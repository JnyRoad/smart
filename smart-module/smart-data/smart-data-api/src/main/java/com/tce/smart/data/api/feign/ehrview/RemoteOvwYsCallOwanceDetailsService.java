package com.tce.smart.data.api.feign.ehrview;


import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.OvwYsCallOwanceDetailsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

/**
 * 补贴查询接口
 * @author QIPEI
 *
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteOvwYsCallOwanceDetailsService {


	/**
	 * 补贴查询
	 * @param badge 员工号
	 * @param xtype 补贴类型
	 * @return
	 */
    @GetMapping("/ovwYsCallOwanceDetail/get")
    Result<OvwYsCallOwanceDetailsDTO> getInfo(@RequestParam("badge") String badge, @RequestParam("xtype") Integer xtype);

	/**
	 * 补贴查询
	 * @param badge 员工号
	 * @param xtype 补贴类型
	 * @return
	 */
	@GetMapping("/ovwYsCallOwanceDetail/time/get")
	Result<Boolean> getInfoByTime(@RequestParam("badge") String badge, @RequestParam("xtype") Integer xtype);

	/**
	 * 补贴查询
	 * @param xtype 补贴类型
	 * @return
	 */
	@GetMapping("/ovwYsCallOwanceDetail/time/get/list")
	Result<List<OvwYsCallOwanceDetailsDTO>> getInfoByTimeList(@RequestParam("xtype") Integer xtype);

	/**
	 * 餐补查询
	 * @return
	 */
	@GetMapping("/ovwYsCallOwanceDetail/get/byBadge")
	Result<Boolean> getInfoByBadge(@RequestParam("badge") String badge, @RequestHeader(SecurityConstants.FROM) String from);

}
