package com.tce.smart.platform.api.dto.req.print;

import lombok.Data;
import com.fasterxml.jackson.databind.JsonNode;

/** 打印模板管理请求；权限与领域不变量统一在服务端校验。 */
@Data
public class PrintTemplateRequest {
    private String parkId;
    private String templateKey;
    private String name;
    private String printItemType;
    private String personType;
    private String classificationCode;
    private String faceRole;
    private Integer sideCount;
    private Long draftRevision;
    private JsonNode layoutJson;
    private JsonNode fieldSchemaJson;
    private JsonNode pageSpecJson;
    private JsonNode resourceManifest;
}
