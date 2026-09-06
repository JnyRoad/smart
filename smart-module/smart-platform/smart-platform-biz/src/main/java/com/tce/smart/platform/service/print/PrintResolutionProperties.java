package com.tce.smart.platform.service.print;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.*;
/** DHR 职层作为候选，只有园区业务已确认的字典才能用于厂牌等级。 */
@Data
@Component
@ConfigurationProperties(prefix="smart.print.resolution")
public class PrintResolutionProperties {
 private Map<String,GradeDictionary> employeeGrades=new LinkedHashMap<>();
 @Data public static class GradeDictionary { private boolean confirmed; private Map<String,String> codes=new LinkedHashMap<>(); }
 public Map<String,String> confirmedGrades(String parkId) {
  GradeDictionary dictionary=employeeGrades.get(parkId);
  if(dictionary==null||!dictionary.isConfirmed()||dictionary.getCodes()==null||dictionary.getCodes().isEmpty()) throw unmapped();
  for(Map.Entry<String,String> entry:dictionary.getCodes().entrySet()) if(entry.getKey()==null||!entry.getKey().matches("[A-Za-z0-9_][A-Za-z0-9_.:-]{0,63}")||"null".equalsIgnoreCase(entry.getKey())||"ALL".equalsIgnoreCase(entry.getKey())||entry.getValue()==null||entry.getValue().trim().isEmpty()) throw unmapped();
  return Collections.unmodifiableMap(dictionary.getCodes());
 }
 private static PrintApiException unmapped(){return new PrintApiException(422,"EMPLOYEE_GRADE_UNMAPPED","本园区 DHR 厂牌等级映射尚未确认或不完整");}
}
