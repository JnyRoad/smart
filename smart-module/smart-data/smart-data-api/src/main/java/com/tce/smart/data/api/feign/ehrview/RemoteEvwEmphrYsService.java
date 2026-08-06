package com.tce.smart.data.api.feign.ehrview;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.EvwEmphrYsDTO;
import com.tce.smart.data.api.dto.ehrview.resp.EvwEmphrYsBlackRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.EvwEmphrYsRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.YsLeaveRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 *
 * @date 2018/6/22
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteEvwEmphrYsService {
	/**
	 * 根据 员工号badge 获取员工基本信息
	 *
	 * @param badge
	 * @param from     调用标志
	 * @return Result
	 */
	@GetMapping("/emphr/ys/info")
    Result<EvwEmphrYsRespDTO> info(@RequestParam("badge") String badge, @RequestHeader(SecurityConstants.FROM) String from);
	/**
	 * 根据 员工号badge 获取员工基本信息(离职用)
	 *
	 * @param badge
	 * @param from     调用标志
	 * @return Result
	 */
	@GetMapping("/emphr/ys/leave")
    Result<YsLeaveRespDTO> leave(@RequestParam("badge") String badge, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 根据BU id获取员工
	 * @param compId BU id
	 * @param from
	 * @return
	 */
	@GetMapping("/emphr/ys/getByCompId")
    Result<List<EvwEmphrYsDTO>> getByCompId(@RequestParam("compId") Integer compId,
										 @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 根据BU id获取在职员工
	 * @param compId BU id
	 * @param from
	 * @return
	 */
	@GetMapping("/emphr/ys/getInStaffByCompId")
    Result<List<EvwEmphrYsDTO>> getInStaffByCompId(@RequestParam("compId") Integer compId,
												   @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 获取黑名单列表
	 * @param certNo
	 * @param from
	 * @return
	 */
	@GetMapping("/emphr/ys/getBlack")
    Result getBlack(@RequestParam("current") long current,@RequestParam("size") long size,@RequestParam("cerNo") String cerNo,@RequestParam("name") String name,
										 @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 获取黑名单list
	 * @param cerNo
	 * @param name
	 * @param from
	 * @return
	 */
	@GetMapping("/emphr/ys/getBlackInfo")
    Result<List<EvwEmphrYsBlackRespDTO>> getBlackInfo(@RequestParam("cerNo") String cerNo,
		@RequestHeader(SecurityConstants.FROM) String from);



}
