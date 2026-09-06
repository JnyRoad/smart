package com.tce.smart.platform.service.print;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.*;
/** 配置只保存令牌SHA-256及授权范围；空配置默认拒绝设备身份。 */
@Getter @Setter @Component @ConfigurationProperties(prefix="smart.print.client")
public class PrintClientProperties {
 private List<Credential> credentials=new ArrayList<>();
 @Getter @Setter public static class Credential {private String deviceIdentity;private String tokenSha256;private Set<String> parkIds=new HashSet<>();private Set<String> printerProfileIds=new HashSet<>();}
}
