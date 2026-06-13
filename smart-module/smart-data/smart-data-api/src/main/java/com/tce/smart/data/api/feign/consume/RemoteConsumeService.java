package com.tce.smart.data.api.feign.consume;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.consume.req.QueryConsumeReqDto;
import com.tce.smart.data.api.dto.consume.resp.ConsumeRecordRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.math.BigDecimal;

/**
 * 短信服务
 *
 * @author mingkai.wu
 * @date 2019-05-15 18:54:18
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteConsumeService {

	/**
	 * 根据员工号分页查询公司账户刷卡记录
	 *
	 * @param queryConsumeReDto 查询条件
	 * @param from              调用方式
	 * @return 消费记录
	 */
	@PostMapping("/consume/record/pub/page")
	Result<Page<ConsumeRecordRespDTO>> listPubPage(@RequestBody QueryConsumeReqDto queryConsumeReDto,
												   @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 按时间统计公司账户消费笔数
	 *
	 * @param queryConsumeReDto 查询条件
	 * @param from              调用方式
	 * @return 消费笔数
	 */
	@PostMapping("/consume/record/pub/count")
	Result<BigDecimal> countPubConsume(@RequestBody QueryConsumeReqDto queryConsumeReDto,
									   @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 根据员工号分页查询个人账户刷卡记录
	 *
	 * @param queryConsumeReDto 分页信息
	 * @param from              调用方式
	 * @return 消费记录
	 */
	@PostMapping("/consume/record/person/page")
	Result<Page<ConsumeRecordRespDTO>> listPrivPage(@RequestBody QueryConsumeReqDto queryConsumeReDto,
													@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 按时间统计个人账户消费笔数
	 *
	 * @param queryConsumeReDto 查询条件
	 * @param from              调用方式
	 * @return 消费笔数
	 */
	@PostMapping("/consume/record/person/count")
	Result<BigDecimal> countPrivConsume(@RequestBody QueryConsumeReqDto queryConsumeReDto,
										@RequestHeader(SecurityConstants.FROM) String from);
}
