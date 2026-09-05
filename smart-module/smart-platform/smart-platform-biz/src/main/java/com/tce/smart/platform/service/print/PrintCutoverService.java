package com.tce.smart.platform.service.print;

import org.springframework.stereotype.Service;
import java.util.*;

/** 只公布通道开关，不读取人员或设备；暂停新任务不把不明结果转投旧链路。 */
@Service
public class PrintCutoverService {
    private final PrintFeatureProperties feature;
    private final PrintExecutionProperties execution;
    private final PrintCutoverProperties cutover;
    public PrintCutoverService(PrintFeatureProperties feature,PrintExecutionProperties execution,PrintCutoverProperties cutover) {this.feature=feature;this.execution=execution;this.cutover=cutover;}
    public Map<String,Object> status(String park) {
        if(park==null || !park.matches("[1-9][0-9]{0,9}"))throw new PrintApiException(422,"PRINT_PARK_REQUIRED","需要明确的园区标识");
        boolean enabled=feature.isEnabled()&&execution.isEnabled();
        boolean selected=cutover.getTemplateParkIds().contains(park);
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("visitorMode",selected?(enabled?"TEMPLATE":"PAUSED"):"LEGACY");
        result.put("legacyVisitorAllowed",!selected);
        result.put("newJobCreationEnabled",enabled);
        result.put("revision",PrintJson.hash(result));
        return result;
    }
}
