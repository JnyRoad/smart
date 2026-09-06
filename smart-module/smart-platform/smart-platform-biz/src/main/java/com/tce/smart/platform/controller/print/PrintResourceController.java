package com.tce.smart.platform.controller.print;

import com.tce.smart.platform.service.print.PrintResourceService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/** 独立授权的图片上传与下载入口；不使用新闻附件公开地址。 */
@RestController
@RequestMapping("/print/v1/resources")
public class PrintResourceController {
    private final PrintResourceService service;
    public PrintResourceController(PrintResourceService service) { this.service=service; }
    @PostMapping public ResponseEntity<?> upload(@RequestParam(required=false) String parkId,@RequestHeader("Content-Type") String type,@RequestHeader(value="X-Print-Resource-Purpose",defaultValue="BACKGROUND") String purpose,HttpServletRequest request) throws IOException {
        return ResponseEntity.status(201).body(PrintTemplateController.response(service.upload(parkId,type,purpose,request.getInputStream()),request));
    }
    @GetMapping("/{id}") public ResponseEntity<byte[]> download(@PathVariable String id,@RequestParam(required=false) String parkId) {
        PrintResourceService.Download result=service.download(id,parkId);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(result.getResource().getMediaType())).header("Cache-Control","no-store").header("X-Content-Type-Options","nosniff").header("X-Artifact-Sha256",result.getResource().getContentHash()).body(result.getBytes());
    }
}
