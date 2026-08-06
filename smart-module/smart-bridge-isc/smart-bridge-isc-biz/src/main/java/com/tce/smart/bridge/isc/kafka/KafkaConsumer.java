package com.tce.smart.bridge.isc.kafka;

import cn.hutool.json.JSONUtil;
import com.tce.smart.bridge.isc.core.service.ExceptionLogService;
import com.tce.smart.bridge.isc.entity.Photo;
import com.tce.smart.bridge.isc.service.HBaseFileService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.UUIDUtils;
import com.tce.smart.dispatcher.api.dto.resp.BridgeDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import java.util.Objects;

/**
 * @Description: TODO
 * @ProjectName smart-bridge
 * @ClassName: KafkaConsumer
 * @Author jinbo
 * @Date 2019/9/29
 */
@Slf4j
@Component
public class KafkaConsumer {
	@Value("${smart.park-id}")
	private Integer parkId;
	@Autowired
	private HBaseFileService hBaseFileService;
	@Autowired
	private ExceptionLogService exceptionLogService;
	@Autowired
	private RemoteDispatcherService remoteDispatcherService;

	/**
	 * 处理 园区C++服务发回的信息
	 *
	 * @param record
	 */
	@KafkaListener(topics = "${smart.kafka.consumer.image-topic}", groupId = "${smart.kafka.consumer.image-group-id}")
	public void imageHandle(ConsumerRecord<String, String> record) {
		try {
			Photo photo = JSONUtil.toBean(record.value(), Photo.class);
			hBaseFileService.save(photo.getPhotoId(), Base64Utils.decodeFromString(photo.getPhotoData()), photo.getPhotoType());
			log.info("存储图片：id={}", photo.getPhotoId());
		}catch (Exception e){
			log.error("图片存储异常：{}", e.getMessage(), e);
		}
	}

	/**
	 * 处理 园区C++服务发回的信息
	 *
	 * @param record
	 */
	@KafkaListener(topics = "${smart.kafka.consumer.event-topic}", groupId = "${smart.kafka.consumer.event-group-id}")
	public void eventHandle(ConsumerRecord<String, String> record) {
		BridgeDTO<String> bridgeDTO = null;
		try {
			long start = DateUtils.toEpochMilli();
			if (log.isDebugEnabled()) {
				log.debug("收到-Kafka消息：{}", record.toString());
			}
			EventEnum key = EventEnum.key(record.key());

			if (key.equals(EventEnum.UNKNOWN)) {
				throw new TCEException("未定义的key：" + record.key());
			}

			bridgeDTO = new BridgeDTO<>();
			bridgeDTO.setEventId(UUIDUtils.create());
			bridgeDTO.setEventType(key.getCode());
			bridgeDTO.setParkId(parkId);
			bridgeDTO.setData(record.value());
			Result<Boolean> result = remoteDispatcherService.handle(bridgeDTO, SecurityConstants.FROM_IN,
					SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
			log.info("处理-Kafka消息，key：{} - {}，EventId：{}，结果：{}，耗时：{}ms", key.getKey(), key.getDesc(), bridgeDTO.getEventId(), result.isSuccess(), DateUtils.toEpochMilli() - start);
			if (!result.isSuccess()) {
				throw new TCEException("Dispatcher处理失败");
			}
		} catch (Exception e) {
			log.error("处理-Kafka消息出现异常，{}", e.getMessage(), e);
			if (Objects.nonNull(bridgeDTO)) {
				log.warn("处理-Kafka消息，EventId：{}，写入数据库异常消息表！", bridgeDTO.getEventId());
				exceptionLogService.insert(record.key(), JSONUtil.toJsonStr(bridgeDTO));
			}
		}
	}

	/**
	 * 处理 园区Java水电表服务发回的信息
	 *
	 * @param record
	 */
	@KafkaListener(topics = "${smart.kafka.consumer.bridge-event-topic}", groupId = "${smart.kafka.consumer.bridge-event-group-id}")
	public void bridgeEventHandle(ConsumerRecord<String, String> record) {
		BridgeDTO<String> bridgeDTO = null;
		try {
			long start = DateUtils.toEpochMilli();
			if (log.isDebugEnabled()) {
				log.debug("收到-水电表集中器Kafka消息：{}", record.toString());
			}
			EventEnum key = EventEnum.key(record.key());
			if (key.equals(EventEnum.UNKNOWN)) {
				throw new TCEException("未定义的水电表集中器key：" + record.key());
			}
			bridgeDTO = new BridgeDTO<>();
			bridgeDTO.setEventId(UUIDUtils.create());
			bridgeDTO.setEventType(key.getCode());
			bridgeDTO.setParkId(parkId);
			bridgeDTO.setData(record.value());
			Result<Boolean> result = remoteDispatcherService.handle(bridgeDTO, SecurityConstants.FROM_IN,
					SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
			log.info("处理-水电表集中器Kafka消息，key：{} - {}，EventId：{}，结果：{}，耗时：{}ms", key.getKey(), key.getDesc(), bridgeDTO.getEventId(), result.isSuccess(), DateUtils.toEpochMilli() - start);
			if (!result.isSuccess()) {
				throw new TCEException("水电表集中器Dispatcher处理失败");
			}
		} catch (Exception e) {
			log.error("处理-水电表集中器Kafka消息出现异常，{}", e.getMessage(), e);
			if (Objects.nonNull(bridgeDTO)) {
				log.warn("处理-水电表集中器Kafka消息，EventId：{}，写入数据库异常消息表！", bridgeDTO.getEventId());
				exceptionLogService.insert(record.key(), JSONUtil.toJsonStr(bridgeDTO));
			}
		}
	}
}
