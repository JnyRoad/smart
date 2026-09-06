package com.tce.smart.platform.core.config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.*;
/** 默认关闭，必须同时配置总开关与精确园区白名单。 */
@Data @Component @ConfigurationProperties(prefix="smart.auth-operation")
public class AuthOperationProperties {
 private boolean enabled=false;
 private Set<Integer> enabledParks=new HashSet<>();
 public boolean enabledForPark(Integer parkId) { return enabled && parkId!=null && enabledParks.contains(parkId); }
}
