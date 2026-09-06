package com.tce.smart.platform.api.dto.req.print;
import lombok.Data;
import lombok.Setter;
import lombok.AccessLevel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
/** 绑定维护请求；职级与目标校验在受权限保护的服务端完成。 */
@Data
public class PrintBindingRequest {
 private String parkId, printItemType, personType, classificationCode, scopeType, scopeId, templateId, pairId, validFrom, validTo;
 private List<String> employeeGradeCodes;
 private Integer priority;
 private Long revision;
 /** 区分PATCH未提供与明确置空，避免意外延长规则的有效期。 */
 @JsonIgnore @Setter(AccessLevel.NONE)
 private boolean validToSpecified;
 public void setValidTo(String value) { this.validTo=value; this.validToSpecified=true; }

}
