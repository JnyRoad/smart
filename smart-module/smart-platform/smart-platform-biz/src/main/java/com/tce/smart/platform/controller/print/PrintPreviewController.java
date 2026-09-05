package com.tce.smart.platform.controller.print;

import com.tce.smart.platform.api.dto.req.print.PrintPreviewRequest;
import com.tce.smart.platform.api.dto.resp.print.PrintApiResponse;
import com.tce.smart.platform.service.print.PrintPreviewService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

/** 预览只返回当前操作员获准的合成制品，不提供直接打印通道。 */
@RestController
@RequestMapping("/print/v1")
public class PrintPreviewController {
    private final PrintPreviewService service;
    public PrintPreviewController(PrintPreviewService service) { this.service=service; }
    @PostMapping("/templates/{id}/preview") public ResponseEntity<?> preview(@PathVariable String id,@RequestBody PrintPreviewRequest body,HttpServletRequest request) { return ResponseEntity.status(202).body(PrintTemplateController.response(service.templatePreview(id,body),request)); }
    @PostMapping("/template-pairs/{id}/preview") public ResponseEntity<?> pairPreview(@PathVariable String id,@RequestBody PrintPreviewRequest body,HttpServletRequest request) { return ResponseEntity.status(202).body(PrintTemplateController.response(service.pairPreview(id,body),request)); }
    @GetMapping("/previews/{id}") public PrintApiResponse<?> detail(@PathVariable String id,HttpServletRequest request) { return PrintTemplateController.response(service.detail(id),request); }
    @GetMapping("/previews/{id}/artifacts/{artifactId}") public ResponseEntity<byte[]> download(@PathVariable String id,@PathVariable String artifactId) { return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).header("Content-Disposition","inline; filename=\""+artifactId+".pdf\"").header("Cache-Control","no-store").header("X-Content-Type-Options","nosniff").body(service.readArtifact(id,artifactId)); }
}
