package com.tce.smart.platform.controller.print;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tce.smart.platform.api.dto.req.print.*;
import com.tce.smart.platform.api.dto.resp.print.PrintApiResponse;
import com.tce.smart.platform.service.print.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
/** 绑定维护与只读人员解析；可信资料源是人员字段的唯一入口。 */
@RestController
@RequestMapping("/print/v1")
public class PrintBindingController {
 private final PrintTemplateResolver resolver;private final PrintSubjectSource source;private final PrintAccessPolicy access;
 public PrintBindingController(PrintTemplateResolver resolver,PrintSubjectSource source,PrintAccessPolicy access){this.resolver=resolver;this.source=source;this.access=access;}
 @GetMapping("/bindings") public PrintApiResponse<?> list(PrintBindingQuery query,HttpServletRequest request){return PrintTemplateController.response(resolver.list(query),request);}
 @GetMapping("/bindings/{id}") public PrintApiResponse<?> detail(@PathVariable String id,HttpServletRequest request){return PrintTemplateController.response(resolver.detail(id),request);}
 @PostMapping("/bindings") public ResponseEntity<?> create(@RequestParam(required=false) String parkId,@RequestBody PrintBindingRequest body,@RequestHeader("Idempotency-Key") String key,HttpServletRequest request){return ResponseEntity.status(201).body(PrintTemplateController.mutation(resolver.create(parkId,body,key),request));}
 @PatchMapping("/bindings/{id}") public PrintApiResponse<?> save(@PathVariable String id,@RequestBody PrintBindingRequest body,@RequestHeader("Idempotency-Key") String key,HttpServletRequest request){return PrintTemplateController.mutation(resolver.save(id,body,key),request);}
 @PostMapping("/bindings/{id}/disable") public PrintApiResponse<?> disable(@PathVariable String id,@RequestBody PrintRevisionRequest body,@RequestHeader("Idempotency-Key") String key,HttpServletRequest request){return PrintTemplateController.mutation(resolver.disable(id,body.getRevision(),key),request);}
 @GetMapping("/binding-options/employee-grades") public PrintApiResponse<?> grades(@RequestParam(required=false) String parkId,HttpServletRequest request){return PrintTemplateController.response(resolver.employeeGrades(parkId),request);}
 @GetMapping("/bindings/resolve") public PrintApiResponse<?> resolve(@RequestParam(required=false) String parkId,@RequestParam String subjectId,@RequestParam(required=false) String subjectType,@RequestParam String printItemType,@RequestParam String personType,@RequestParam String classificationCode,HttpServletRequest request){
  String park=access.resolvePark(parkId);access.require("execute",park);
  String type=subjectType==null?("VISITOR_SLIP".equals(printItemType)?"VISITOR":"STAFF"):subjectType;ObjectNode subject=source.load(park,type,subjectId);
  if(subject==null||!park.equals(subject.path("parkId").asText())||!subjectId.equals(subject.path("subjectId").asText())||!type.equals(subject.path("subjectType").asText()))throw new PrintApiException(403,"PRINT_SCOPE_DENIED","人员源返回了其他业务对象");
  Map<String,String> asserted=new LinkedHashMap<>();asserted.put("printItemType",printItemType);asserted.put("personType",personType);asserted.put("classificationCode",classificationCode);asserted.forEach((key,value)->{if(value!=null&&!value.equals(subject.path(key).asText()))throw new PrintApiException(422,"PRINT_SUBJECT_INVALID","请求分类与可信人员资料不一致");});
  return PrintTemplateController.response(resolver.resolve(park,subject),request);
 }
}
