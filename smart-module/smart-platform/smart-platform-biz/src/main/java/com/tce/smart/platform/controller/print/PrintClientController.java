package com.tce.smart.platform.controller.print;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tce.smart.platform.api.dto.resp.print.PrintApiResponse;
import com.tce.smart.platform.service.print.*;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
/** 专用设备身份入口，不能从管理员会话或请求体取得身份。 */
@RestController @RequestMapping("/api/print-client/v1") public class PrintClientController {
 final PrintClientService clients;public PrintClientController(PrintClientService clients){this.clients=clients;}
 PrintClientIdentity device(){Object p=SecurityContextHolder.getContext().getAuthentication()==null?null:SecurityContextHolder.getContext().getAuthentication().getPrincipal();if(!(p instanceof PrintClientIdentity))throw new PrintApiException(401,"PRINT_DEVICE_AUTHENTICATION_REQUIRED","需要独立设备身份");return (PrintClientIdentity)p;}
 @PostMapping("/claim")public PrintApiResponse<?> claim(@RequestBody ObjectNode b,@RequestHeader("Idempotency-Key")String k,HttpServletRequest r){return PrintTemplateController.mutation(clients.claim(device(),b,k),r);}
 @GetMapping("/claims/{id}/current")public PrintApiResponse<?> current(@PathVariable String id,HttpServletRequest r){return PrintTemplateController.response(clients.current(device(),id),r);}
 @PostMapping("/claims/{id}/renew")public PrintApiResponse<?> renew(@PathVariable String id,@RequestBody ObjectNode b,@RequestHeader("Idempotency-Key")String k,HttpServletRequest r){return PrintTemplateController.mutation(clients.renew(device(),id,b,k),r);}
 @PostMapping("/jobs/{id}/events")public PrintApiResponse<?> event(@PathVariable String id,@RequestBody ObjectNode b,@RequestHeader("Idempotency-Key")String k,HttpServletRequest r){return PrintTemplateController.mutation(clients.event(device(),id,b,k),r);}
 @PostMapping("/jobs/{id}/device-cleared")public PrintApiResponse<?> cleared(@PathVariable String id,@RequestBody ObjectNode b,@RequestHeader("Idempotency-Key")String k,HttpServletRequest r){return PrintTemplateController.mutation(clients.deviceCleared(device(),id,b,k),r);}
 @GetMapping("/jobs/{id}/artifacts/{face}/download")public ResponseEntity<byte[]> artifact(@PathVariable String id,@PathVariable String face){return PrintJobController.pdf(clients.artifact(device(),id,face),id,face);}
}
