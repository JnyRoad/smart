package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.lock.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 远程智能锁平台接口
 *
 */
@FeignClient(value = "smart-lock", url = "${spring.lock.base.url:http://smart-lock:8001}")
public interface RemoteSmartLockService {

	/**
	 * 智能锁分页查询
	 * @param reqDTO
	 * @return
	 */
	@PostMapping(value = "/api/device/page")
	Result devicePage(@RequestBody DevicePageReqDTO reqDTO);

	/**
	 * 智能锁绑定房间
	 * @param reqDTO
	 * @return
	 */
	@PostMapping("/api/device/bind/room")
	Result<Boolean> bindRoom(@RequestBody DeviceBindReqDTO reqDTO);

	/**
	 * 智能锁开门记录查询
	 * @param queryDTO
	 * @return
	 */
	@PostMapping("/api/device/open-door/record/page")
	Result getOpenDoorRecordPage(@RequestBody OpenDoorRecordQueryDTO queryDTO);

	/**
	 * 开门记录导出Excel
	 * @param queryDTO
	 * @return
	 */
	@PostMapping("/api/device/open-door/record/export")
	ResponseEntity<byte[]> exportOpenDoorRecord(@RequestBody OpenDoorRecordQueryDTO queryDTO);

	/**
	 * 根据房间id获得门锁支持的钥匙
	 * @param roomId
	 * @return
	 */
	@GetMapping("/api/device/support/key/{roomId}}")
	Result supportKeyByRoomId(@PathVariable("roomId") Long roomId);

	/**
	 * 智能锁授权分页查询
	 * @param reqDTO
	 * @return
	 */
	@PostMapping("/api/permissions/page")
	Result getLkDevicePermissionsPage(@RequestBody ApiDeviceAuthReqDTO reqDTO);

	/**
	 * 通过id查询设备权限表
	 * @param id
	 * @return
	 */
	@GetMapping("/api/permissions/{id}")
	Result getById(@PathVariable("id") Long id);

	/**
	 * 根据设备ID查找绑定的人员列表
	 *
	 * @param deviceId
	 * @return
	 */
	@GetMapping("/api/permissions/person/{deviceId}")
	Result getPersonsByDeviceId(@PathVariable("deviceId") Long deviceId);

	/**
	 * 批量授权
	 * @param reqDTO
	 * @return
	 */
	@PostMapping("/api/permissions/batch")
	Result<Boolean> addBatch(@RequestBody ApiBatchAuthDTO reqDTO);

	/**
	 * 修改设备权限表
	 *
	 * @param authDTO 设备权限表
	 * @return Result
	 */
	@PostMapping("/api/permissions")
	Result<Boolean> updateById(@RequestBody ApiEditAuthDTO authDTO);

	/**
	 * 通过id删除设备权限表
	 *
	 * @param id id
	 * @return Result
	 */
	@PostMapping("/api/permissions/del/{id}")
	Result<Boolean> removeById(@PathVariable("id") Long id);

	/**
	 * 取消授权
	 *
	 * @param id
	 * @return
	 */
	@PostMapping("/api/permissions/cancelAuth/{id}")
	Result<Boolean> cancelAuth(@PathVariable("id") Long id);

	/**
	 * 授权管理分页查询
	 *
	 * @param queryDTO  分页对象
	 * @param queryDTO
	 * @return
	 */
	@PostMapping("/api/device/task/page")
	Result getTaskPage(@RequestBody ApiDeviceTaskQueryDTO queryDTO);

	/**
	 * 通过人员编号或姓名查询人员
	 * @param queryName
	 * @return
	 */
	@GetMapping("/api/person/search/{queryName}")
	Result getByNumOrName(@PathVariable("queryName") String queryName);

	/**
	 * 重新授权
	 *
	 * @param authDTO 设备权限表
	 * @return Result
	 */
	@PostMapping("/api/permissions/reAuth")
	Result<Boolean> reAuth(@RequestBody ApiEditAuthDTO authDTO);
}
