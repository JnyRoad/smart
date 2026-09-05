package com.tce.smart.platform.controller.print;

import com.tce.smart.platform.api.dto.req.print.*;
import com.tce.smart.platform.api.dto.resp.print.PrintApiResponse;
import com.tce.smart.platform.service.print.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/** 网关移除 platform 前缀后进入本控制器；所有业务权限在服务层执行。 */
@RestController
@RequestMapping("/print/v1")
public class PrintTemplateController {
    private final PrintTemplateService service;
    public PrintTemplateController(PrintTemplateService service) { this.service = service; }
    @GetMapping("/templates") public PrintApiResponse<?> list(PrintListQuery query, HttpServletRequest request) { return response(service.list(query), request); }
    @PostMapping("/templates") public ResponseEntity<?> create(@RequestParam(required = false) String parkId, @RequestBody PrintTemplateRequest body, HttpServletRequest request) { return ResponseEntity.status(201).body(response(service.create(parkId, body), request)); }
    @GetMapping("/templates/{id}") public PrintApiResponse<?> detail(@PathVariable String id, HttpServletRequest request) { return response(service.detail(id), request); }
    @PatchMapping("/templates/{id}") public PrintApiResponse<?> save(@PathVariable String id, @RequestBody PrintTemplateRequest body, HttpServletRequest request) { return response(service.save(id, body), request); }
    @GetMapping("/templates/{id}/versions") public PrintApiResponse<?> versions(@PathVariable String id, HttpServletRequest request) { return response(service.versions(id), request); }
    @PostMapping("/templates/{id}/publish") public ResponseEntity<?> publish(@PathVariable String id, @RequestBody PrintPublishRequest body, @RequestHeader("Idempotency-Key") String key, HttpServletRequest request) { return ResponseEntity.status(201).body(mutation(service.publish(id, body, key), request)); }
    @PostMapping("/templates/{id}/rollback") public PrintApiResponse<?> rollback(@PathVariable String id, @RequestBody PrintRollbackRequest body, @RequestHeader("Idempotency-Key") String key, HttpServletRequest request) { return mutation(service.rollback(id, body, key), request); }
    @GetMapping("/template-pairs") public PrintApiResponse<?> pairs(PrintListQuery query, HttpServletRequest request) { return response(service.listPairs(query), request); }
    @PostMapping("/template-pairs") public ResponseEntity<?> createPair(@RequestParam(required = false) String parkId, @RequestBody PrintPairRequest body, @RequestHeader("Idempotency-Key") String key, HttpServletRequest request) { return ResponseEntity.status(201).body(mutation(service.createPair(parkId, body, key), request)); }
    @GetMapping("/template-pairs/{id}") public PrintApiResponse<?> pair(@PathVariable String id, HttpServletRequest request) { return response(service.pairDetail(id), request); }
    @PatchMapping("/template-pairs/{id}") public PrintApiResponse<?> savePair(@PathVariable String id, @RequestBody PrintPairRequest body, @RequestHeader("Idempotency-Key") String key, HttpServletRequest request) { return mutation(service.savePair(id, body, key), request); }
    @PostMapping("/template-pairs/{id}/archive") public PrintApiResponse<?> archivePair(@PathVariable String id, @RequestBody PrintRevisionRequest body, @RequestHeader("Idempotency-Key") String key, HttpServletRequest request) { return mutation(service.archivePair(id, body.getRevision(), key), request); }
    @GetMapping("/template-versions/{versionId}/resources/{objectId}") public ResponseEntity<byte[]> resource(@PathVariable String versionId, @PathVariable String objectId) {
        Map<String, Object> resource = service.resource(versionId, objectId); return ResponseEntity.ok().contentType(MediaType.parseMediaType((String) resource.get("mediaType"))).header("Cache-Control", "no-store").header("X-Content-Type-Options", "nosniff").header("Content-Disposition", "attachment; filename=\"" + objectId + "\"").header("ETag", "\"" + resource.get("hash") + "\"").body((byte[]) resource.get("bytes"));
    }
    static PrintApiResponse<?> mutation(PrintMutationResult result, HttpServletRequest request) { PrintApiResponse<?> response = response(result.getData(), request); response.setReplayed(result.isReplayed()); return response; }
    static <T> PrintApiResponse<T> response(T data, HttpServletRequest request) { return new PrintApiResponse<>(data, requestId(request)); }
    static String requestId(HttpServletRequest request) { Object id = request.getAttribute("print.requestId"); if (id == null) { id = UUID.randomUUID().toString(); request.setAttribute("print.requestId", id); } return id.toString(); }
}
