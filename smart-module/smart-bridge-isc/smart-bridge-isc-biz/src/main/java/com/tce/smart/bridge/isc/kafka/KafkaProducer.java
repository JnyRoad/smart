package com.tce.smart.bridge.isc.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @Description: TODO
 * @ProjectName smart-bridge
 * @ClassName: KafkaProducer
 * @Author jinbo
 * @Date 2019/10/8
 */
@Slf4j
@Component
public class KafkaProducer {
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	public boolean sendMessage(String topic, String key, final String message) {
		if (log.isDebugEnabled()) {
			log.debug("Kafka-消息发送：message={}，key={}，message={}", topic, key, message);
		}
		try {
			kafkaTemplate.send(topic, key, message);
			Thread.sleep(1);
			return true;
		}catch (Exception e){
			log.error("Kafka-消息发送异常：{}", e.getMessage(), e);
		}
		return false;
	}

}
