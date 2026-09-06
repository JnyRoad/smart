package com.tce.smart.platform.core.entity.print;
import lombok.Data;
import java.sql.Timestamp;
/** 按园区持久化的人员与模板适用规则，发布流程管理表结构。 */
@Data
public class PrintBindingRule {
    private String bindingRuleId;
    private String parkId;
    private String printItemType;
    private String personType;
    private String classificationCode;
    private String scopeType;
    private String scopeId;
    private String templateId;
    private String pairId;
    private String employeeGradeCodesClob;
    private Integer priority;
    private Timestamp validFrom;
    private Timestamp validTo;
    private String status;
    private Long revision;
    private String createdBy;
    private Timestamp createdAt;
    private String updatedBy;
    private Timestamp updatedAt;
}
