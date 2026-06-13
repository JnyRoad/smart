package com.tce.smart.admin.task;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tce.smart.admin.api.entity.SysMoveDataTask;
import com.tce.smart.admin.service.SysMoveDataTaskService;
import com.tce.smart.common.core.constant.enums.DateMoveModuleEnum;
import com.tce.smart.common.core.constant.enums.TimerTaskEnum;
import com.tce.smart.common.core.util.DateUtils;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bytecode.constant.LongConstant;

/**
 * 定时任务服务
 *
 * @author mkwu
 * @date 2019-08-06
 */
@Slf4j
@Component
@Configurable
@EnableScheduling
public class AdminTimerTask {

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private SysMoveDataTaskService sysMoveDataTaskService;

	// 每天00:00分执行
	@Scheduled(cron = "0 0 00 * * ?")
	public void syncEmpPhoto() {
		if (process(TimerTaskEnum.MOVE_ADMIN_DATA)) {
			log.info("===========每00:00定时===========");

			List<SysMoveDataTask> tableTaskList = sysMoveDataTaskService
					.getModuleTaskList(DateMoveModuleEnum.SMART.getCode());

			// 处理转移数据
			if (CollectionUtil.isNotEmpty(tableTaskList)) {
				tableTaskList.forEach(element -> sysMoveDataTaskService.processData(element));
			}
		}
	}

	private boolean process(TimerTaskEnum timerTask) {
		Boolean exists = redisTemplate.hasKey(timerTask.getKey());
		if (Objects.nonNull(exists) && !exists) {
			redisTemplate.opsForValue().set(timerTask.getKey(), timerTask.getDesc(), LongConstant.ONE.ordinal(),
					TimeUnit.MINUTES);
			log.info("开始执行：" + timerTask.getDesc() + " " + DateUtils.now());
			return true;
		}
		return false;
	}

}