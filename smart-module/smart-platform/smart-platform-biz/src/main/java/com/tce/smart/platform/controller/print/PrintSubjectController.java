package com.tce.smart.platform.controller.print;

import com.tce.smart.platform.api.dto.resp.print.PrintApiResponse;
import com.tce.smart.platform.service.print.PrintSubjectSource;
import com.tce.smart.platform.service.print.PrintJson;
import com.tce.smart.platform.service.print.PrintApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.*;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

/** 明确命名空间的园区人员搜索；不接收浏览器提供的人事内容。 */
@RestController
@RequestMapping("/print/v1/print-subjects")
public class PrintSubjectController {
    private final PrintSubjectSource source;
    public PrintSubjectController(PrintSubjectSource source) { this.source=source; }
    @GetMapping public PrintApiResponse<?> search(@RequestParam(required=false) String parkId,
            @RequestParam String subjectType,@RequestParam(defaultValue="") String keyword,
            @RequestParam(defaultValue="1") int current,@RequestParam(defaultValue="20") int size,HttpServletRequest request) {
        return PrintTemplateController.response(source.search(parkId,subjectType,keyword,current,size),request);
    }
    /** 从旧入口接收记录ID，先验证整批形状，再逐人经过原有授权资料源。 */
    @PostMapping("/selection") public PrintApiResponse<?> selection(@RequestBody ObjectNode body,HttpServletRequest request) {
        PrintJson.limit(body,32768);
        Set<String> rootKeys=new HashSet<>(Arrays.asList("parkId","subjects"));
        body.fieldNames().forEachRemaining(key->{if(!rootKeys.contains(key))throw invalidSelection();});
        JsonNode subjects=body.path("subjects");
        if(!subjects.isArray()||subjects.size()<1||subjects.size()>100)throw invalidSelection();
        Set<String> seen=new HashSet<>();
        for(JsonNode item:subjects) {
            if(!item.isObject()||item.size()!=2||!item.path("subjectType").isTextual()||!item.path("subjectId").isTextual()
                ||!Arrays.asList("ADMITTANCE","ADMITTANCE_COMPANION","VISITOR","VISITOR_COMPANION").contains(item.path("subjectType").asText())
                ||!item.path("subjectId").asText().matches("[1-9][0-9]{0,18}")
                ||!seen.add(item.path("subjectType").asText()+":"+item.path("subjectId").asText()))throw invalidSelection();
        }
        ObjectNode result=PrintJson.object();ArrayNode records=result.putArray("records");
        for(JsonNode item:subjects) {
            ObjectNode subject=source.load(body.path("parkId").asText(null),item.path("subjectType").asText(),item.path("subjectId").asText());
            records.addObject().put("subjectId",subject.path("subjectId").asText()).put("subjectType",subject.path("subjectType").asText())
                .put("displayName",subject.at("/fields/visitorName").asText());
        }
        return PrintTemplateController.response(result,request);
    }
    private static PrintApiException invalidSelection(){return new PrintApiException(422,"INVALID_REQUEST","访客选择格式无效");}
}
