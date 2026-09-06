package com.tce.smart.platform.service.print;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.*;

/** 园区仅在联合验收后加入切换清单；留空保留旧访客入口。 */
@Data @Component @ConfigurationProperties(prefix="smart.print.cutover")
public class PrintCutoverProperties {
    private Set<String> templateParkIds=new HashSet<>();
}
