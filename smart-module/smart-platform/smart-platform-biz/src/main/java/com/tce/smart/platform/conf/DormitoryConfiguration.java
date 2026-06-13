package com.tce.smart.platform.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: DormitoryConfiguration
 * @Author qipei
 * @Date 2020/2/19
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.dormitory")
public class DormitoryConfiguration {

	private Integer datenum;

}
