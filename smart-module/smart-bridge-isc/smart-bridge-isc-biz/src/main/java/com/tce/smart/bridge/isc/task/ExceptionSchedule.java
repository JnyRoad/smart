package com.tce.smart.bridge.isc.task;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.bridge.isc.core.entity.ExceptionLog;
import com.tce.smart.bridge.isc.core.service.ExceptionLogService;
import com.tce.smart.common.core.constant.NumberConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.dispatcher.api.dto.resp.BridgeDTO;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description: TODO
 * @ProjectName smart-bridge
 * @ClassName: ImageScheduling
 * @Author jinbo
 * @Date 2019/10/8
 */
@Slf4j
@Component
public class ExceptionSchedule {
	@Autowired
	private ExceptionLogService exceptionLogService;
	@Autowired
	private RemoteDispatcherService remoteDispatcherService;
	//10分钟触发一次
	@Scheduled(fixedDelayString = "${smart.exception-log.retry:600}")
	@SuppressWarnings("unchecked")
	public void retry() {
		try {
			log.info("开始-重试异常消息任务...");
			long start = DateUtils.toEpochMilli();
			Integer total = exceptionLogService.count(Wrappers.emptyWrapper());
			log.info("执行-重试异常消息任务，共{}条数据需要清理", total);
			Integer id = NumberConstants.ZERO;
			Integer size = NumberConstants.ONE;
			List<ExceptionLog> exceptionLogs = exceptionLogService.getList(id, size);
			while (CollectionUtils.isNotEmpty(exceptionLogs)) {
				for (ExceptionLog exceptionLog : exceptionLogs) {
					try {
						BridgeDTO<String> bridgeDTO = JSONUtil.toBean(exceptionLog.getMessage(), BridgeDTO.class);
						Result<Boolean> result = remoteDispatcherService.handle(bridgeDTO, SecurityConstants.FROM_IN);
						log.info("执行-重试异常消息任务，key：{}，EventId：{}，结果：{}，耗时：{}ms", exceptionLog.getEventType(), bridgeDTO.getEventId(), result.isSuccess(), DateUtils.toEpochMilli() - start);
						if (result.isSuccess()) {
							exceptionLog.deleteById();
						}
					} catch (Exception e) {
						log.error("执行-重试异常消息任务出现异常，{}", e.getMessage(), e);
					}
					id = exceptionLog.getId();
				}
				exceptionLogs = exceptionLogService.getList(id, size);
			}
			log.info("完成-重试异常消息任务, 耗时：{}ms", DateUtils.toEpochMilli() - start);
		} catch (Exception e) {
			log.error("执行异常-重试异常消息任务：{}", e.getMessage(), e);
		}
	}
}
