package com.tce.smart.platform.api.dto.req.print;
import lombok.Data;
/** 绑定查询仅允许本园区分页。 */
@Data
public class PrintBindingQuery {
 private String parkId, printItemType, personType, classificationCode, scopeType, scopeId, status;
 private Integer current=1, size=20;
}
