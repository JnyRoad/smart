package com.tce.smart.platform.service.print;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.*;

/** 人员源的明确业务裁定；未知访客分类与照片存储域一律拒绝，不跨域兜底。 */
@Data
@Component
@ConfigurationProperties(prefix="smart.print.subject")
public class PrintSubjectProperties {
    /** 园区到旧访客分类策略：NORMAL 或 SECURITY；默认未确认。 */
    private Map<String,String> legacyClassification = new LinkedHashMap<>();
    /** 园区到新申请策略：仅明确开启 AUTHORITY_MAPPING。 */
    private Map<String,String> admittanceClassification = new LinkedHashMap<>();
    /** 园区允许提前预印的秒数，缺省零。 */
    private Map<String,Long> earlyPrintSeconds = new LinkedHashMap<>();
    private Map<String,PhotoDomain> photos = new LinkedHashMap<>();
    private int maxPhotoBytes = 5 * 1024 * 1024;
    private long maxPhotoPixels = 16000000;
    @Data public static class PhotoDomain {
        /** NULL 为数据库空园区，0 为员工共享域，PARK 为人员业务园区，或明确的数字园区。 */
        private String storageDomain;
        private List<Integer> allowedTypes = new ArrayList<>();
    }
}
