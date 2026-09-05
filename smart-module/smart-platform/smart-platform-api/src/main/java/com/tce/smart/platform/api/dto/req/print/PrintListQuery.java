package com.tce.smart.platform.api.dto.req.print;

import lombok.Data;
import com.fasterxml.jackson.databind.JsonNode;

/** 打印模板管理请求；权限与领域不变量统一在服务端校验。 */
@Data
public class PrintListQuery {
    private String parkId;
    private String printItemType;
    private String personType;
    private String classificationCode;
    private String faceRole;
    private String status;
    private Integer current = 1;
    private Integer size = 20;
}
