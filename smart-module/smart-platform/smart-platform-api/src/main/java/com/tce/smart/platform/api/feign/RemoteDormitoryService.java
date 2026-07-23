package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.SmtDormitoryReqDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.SearchDormitoryRoomDetailReqDTO;
import com.tce.smart.platform.api.dto.resp.DormitoryFloorRespDTO;
import com.tce.smart.platform.api.dto.resp.DormitoryRoomDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.RoomBedRespDTO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Title: RemoteDormitoryService
 * @Auther: guohongtai
 * @Date: 2020-10-14 21:29
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteDormitoryService {

	/**
	 * 根据园区ID查询宿舍楼和楼层
	 * @return Result
	 */
	@PostMapping("/dormitory/queryDormitory")
	Result<List<DormitoryFloorRespDTO>> queryDormitory(@RequestBody SmtDormitoryReqDTO smtDormitory, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 根据楼层ID查询房间和床位
	 * @return Result
	 */
	@PostMapping("/dormitory/room/queryRoom")
	Result<List<RoomBedRespDTO>> queryRoom(@RequestBody SearchDormitoryRoomDetailReqDTO smtDormitoryRoom, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 根据员工工号查询入住信息
	 * @param staffBadge
	 * @return
	 */
	@GetMapping("/dormitory/staff/internal/roomDetail/{staffBadge}")
	Result<DormitoryRoomDetailRespDTO> getStaffRoomInfo(
			@PathVariable("staffBadge") String staffBadge,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	@GetMapping("/dormitory/staff/roomList/{staffBadge}")
	Result<List<DormitoryRoomDetailRespDTO>> getStaffRoomInfoList(@PathVariable("staffBadge") String staffBadge, @RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/dormitory/staff/roomList/{staffBadge}")
	Result<List<DormitoryRoomDetailRespDTO>> getSimpleStaffRoomList(@PathVariable("staffBadge") String staffBadge, @RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/dor/quit/dealy/quit")
	Result<Boolean> dealyQuit(@RequestHeader(SecurityConstants.FROM) String from);

}
