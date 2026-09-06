package com.tce.smart.platform.controller.print;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tce.smart.platform.api.dto.resp.print.PrintApiResponse;
import com.tce.smart.platform.service.print.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
/** 管理端任务及真实人员预览入口，鉴权和幂等均在服务内执行。 */
@RestController @RequestMapping("/print/v1")
public class PrintJobController {
 final PrintJobService jobs;final PrintJobPreviewService previews;
 public PrintJobController(PrintJobService jobs,PrintJobPreviewService previews){this.jobs=jobs;this.previews=previews;}
 @PostMapping("/print-jobs") public ResponseEntity<?> create(@RequestBody ObjectNode body,@RequestHeader("Idempotency-Key")String key,HttpServletRequest r){return ResponseEntity.status(202).body(PrintTemplateController.mutation(jobs.create(body,key),r));}
 @PostMapping("/print-jobs/batch") public ResponseEntity<?> batch(@RequestBody ObjectNode body,@RequestHeader("Idempotency-Key")String key,HttpServletRequest r){return ResponseEntity.status(202).body(PrintTemplateController.mutation(jobs.batch(body,key),r));}
 @PostMapping("/print-jobs/preview") public PrintApiResponse<?> preview(@RequestBody ObjectNode body,HttpServletRequest r){return PrintTemplateController.response(previews.preview(body),r);}
 @GetMapping("/print-jobs/previews/{id}") public PrintApiResponse<?> previewDetail(@PathVariable String id,HttpServletRequest r){return PrintTemplateController.response(previews.detail(id),r);}
 @GetMapping("/print-jobs/previews/{id}/artifacts/{artifact}") public ResponseEntity<byte[]> previewArtifact(@PathVariable String id,@PathVariable String artifact){return pdf(previews.artifact(id,artifact),null,null);}
 @GetMapping("/print-jobs") public PrintApiResponse<?> list(com.tce.smart.platform.api.dto.req.print.PrintJobQuery query,HttpServletRequest r){return PrintTemplateController.response(jobs.list(query),r);}
 @GetMapping("/print-jobs/{id}") public PrintApiResponse<?> detail(@PathVariable String id,HttpServletRequest r){return PrintTemplateController.response(jobs.detail(id),r);}
 @PostMapping("/print-jobs/{id}/cancel") public PrintApiResponse<?> cancel(@PathVariable String id,@RequestBody ObjectNode body,@RequestHeader("Idempotency-Key")String key,HttpServletRequest r){return PrintTemplateController.mutation(jobs.cancel(id,body,key),r);}
 @PostMapping("/print-jobs/{id}/flip-confirmation") public PrintApiResponse<?> flip(@PathVariable String id,@RequestBody ObjectNode body,@RequestHeader("Idempotency-Key")String key,HttpServletRequest r){return PrintTemplateController.mutation(jobs.flip(id,body,key),r);}
 @PostMapping("/print-jobs/{id}/output-check") public PrintApiResponse<?> output(@PathVariable String id,@RequestBody ObjectNode body,@RequestHeader("Idempotency-Key")String key,HttpServletRequest r){return PrintTemplateController.mutation(jobs.outputCheck(id,body,key),r);}
 @GetMapping("/print-jobs/{id}/events") public PrintApiResponse<?> events(@PathVariable String id,HttpServletRequest r){return PrintTemplateController.response(jobs.events(id),r);}
 @GetMapping("/print-jobs/{id}/artifacts/{face}/download") public ResponseEntity<byte[]> artifact(@PathVariable String id,@PathVariable String face){return pdf(jobs.artifact(id,face),id,face);}
 static ResponseEntity<byte[]> pdf(byte[] bytes,String job,String face){ResponseEntity.BodyBuilder builder=ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).contentLength(bytes.length).header("Cache-Control","no-store").header("X-Content-Type-Options","nosniff").header("X-Artifact-Sha256",PrintJson.hashBytes(bytes)).header("ETag","\""+PrintJson.hashBytes(bytes)+"\"");if(job!=null)builder.header("X-Job-Id",job).header("X-Face",face);return builder.body(bytes);}
}
