package com.tce.smart.algorithm.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @ClassName: RecordProperties
 * @Package com.tce.smart.yunxun.algorithm.config.properties
 * @Description:
 * @Author wuxinjian
 * @Date 2019-10-10 14:22
 * @Version V1.0
 */
@Component
@ConfigurationProperties(prefix = RecordProperties.PREFIX)
@Data
@RefreshScope
public class RecordProperties {

	static final String PREFIX = "record";

    private Boolean save;

}
