package com.tce.smart.platform.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: VisitorConfiguration
 * @Author jinbo
 * @Date 2019/5/24
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.visitor")
public class VisitorConfiguration {

	private Integer overtimeOffsetHour = 0;

	private Integer arrivedOffsetHour = 0;

	private Integer putOffsetHour = 0;

	private Boolean arrivedSend = Boolean.TRUE;

	private Integer arrivedSendMessageOffsetMinute = 30;

	private Boolean preSend = Boolean.TRUE;

	private Integer preSendMessageOffsetMinute = 30;

}
