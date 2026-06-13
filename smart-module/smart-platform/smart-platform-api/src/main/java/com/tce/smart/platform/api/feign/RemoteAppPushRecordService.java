package com.tce.smart.platform.api.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.QueryAppMsgRecReqDTO;
import com.tce.smart.platform.api.dto.resp.QueryAppMsgRecRespDTO;
import com.tce.smart.platform.api.dto.SmtMsgRecordDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 警报记录
 *
 * @author Lenovo
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteAppPushRecordService {

	/**
	 * 设置App消息为已读
	 *
	 * @param recordId 消息记录ID
	 * @param from 调用方式
	 * @return
	 */
	@GetMapping("/appmsg/update/read")
    Result<Boolean> changeRecordToRead(@RequestParam("recordId") Integer recordId,
                                       @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 统计App消息数
	 *
	 * @param queryAppMsgRecDTO App消息推送记录查询条件
	 * @return
	 */
	@PostMapping("/appmsg/count/app")
    Result<QueryAppMsgRecRespDTO> countAppMsg(@RequestBody QueryAppMsgRecReqDTO queryAppMsgRecDTO,
                                              @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 分页App消息推送记录查询
	 *
	 * @param current            当前页
	 * @param size
	 * @param queryAppMsgRecDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/appmsg/page")
    Result<Page<SmtMsgRecordDTO>> lisetAppMsgByPge(@RequestParam("current") final long current,
                                                   @RequestParam("size") final long size, @RequestBody QueryAppMsgRecReqDTO queryAppMsgRecDTO,
                                                   @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 删除App消息
	 *
	 * @param recordId 消息记录ID
	 * @param from 调用方式
	 * @return
	 */
	@PostMapping("/appmsg/delete")
    Result<Boolean> deleteMsg(@RequestParam("recordId") Integer recordId,
                              @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 删除所有App消息
	 *
	 * @param from 调用方式
	 * @return
	 */
	@PostMapping("/appmsg/delete/all")
    Result<Boolean> deleteAllMsg(@RequestBody QueryAppMsgRecReqDTO queryAppMsgRecDTO,
                                 @RequestHeader(SecurityConstants.FROM) String from);
	/**
	 * 设置App消息所有为已读
	 *
	 * @param queryAppMsgRecDTO queryAppMsgRecDTO
	 * @param fromIn 调用方式
	 * @return
	 */
	@PostMapping("/appmsg/update/all/read")
    Result<Boolean> changeAllRecordToRead(@RequestBody QueryAppMsgRecReqDTO queryAppMsgRecDTO, @RequestHeader(SecurityConstants.FROM) String fromIn);
}
