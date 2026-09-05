package com.tce.smart.platform.controller.print;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tce.smart.platform.api.dto.resp.print.PrintApiResponse;
import com.tce.smart.platform.service.print.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
/** 打印机档案只能按园区设备管理权限维护。 */
@RestController @RequestMapping("/print/v1/printer-profiles") public class PrintPrinterController {
 final PrintPrinterService service;public PrintPrinterController(PrintPrinterService service){this.service=service;}
 @GetMapping("/options") public PrintApiResponse<?> options(@RequestParam(required=false)String parkId,@RequestParam(defaultValue="1")int page,@RequestParam(defaultValue="20")int size,HttpServletRequest r){return PrintTemplateController.response(service.options(parkId,page,size),r);}
 @GetMapping public PrintApiResponse<?> list(@RequestParam(required=false)String parkId,@RequestParam(defaultValue="1")int page,@RequestParam(defaultValue="20")int size,HttpServletRequest r){return PrintTemplateController.response(service.list(parkId,page,size),r);}
 @GetMapping("/{id}")public PrintApiResponse<?> detail(@PathVariable String id,HttpServletRequest r){return PrintTemplateController.response(service.detail(id),r);}
 @PostMapping public ResponseEntity<?> create(@RequestBody ObjectNode b,@RequestHeader("Idempotency-Key")String k,HttpServletRequest r){return ResponseEntity.status(201).body(PrintTemplateController.mutation(service.save(null,b,k),r));}
 @PatchMapping("/{id}")public PrintApiResponse<?> save(@PathVariable String id,@RequestBody ObjectNode b,@RequestHeader("Idempotency-Key")String k,HttpServletRequest r){return PrintTemplateController.mutation(service.save(id,b,k),r);}
 @PostMapping("/{id}/verification")public PrintApiResponse<?> verify(@PathVariable String id,@RequestBody ObjectNode b,@RequestHeader("Idempotency-Key")String k,HttpServletRequest r){return PrintTemplateController.mutation(service.save(id,b,k),r);}
 @PostMapping("/{id}/disable")public PrintApiResponse<?> disable(@PathVariable String id,@RequestBody ObjectNode b,@RequestHeader("Idempotency-Key")String k,HttpServletRequest r){return PrintTemplateController.mutation(service.disable(id,b,k),r);}
}
