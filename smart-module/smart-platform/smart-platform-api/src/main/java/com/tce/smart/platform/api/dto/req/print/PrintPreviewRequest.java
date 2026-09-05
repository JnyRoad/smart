package com.tce.smart.platform.api.dto.req.print;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

/** 预览仅接收合成字段；真实人员预览须另接业务身份及照片授权。 */
@Data
public class PrintPreviewRequest {
    private String versionId;
    private Long revision;
    private JsonNode sampleData;
}
