package com.tce.smart.platform.controller.print;

import com.tce.smart.platform.service.print.PrintCutoverService;
import com.tce.smart.platform.api.dto.resp.print.PrintApiResponse;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.*;

/** 旧自助访客页仅读公开通道状态，正式模板任务仍须平台操作员鉴权。 */
@RestController @RequestMapping("/print/v1/cutover")
public class PrintCutoverController {
    private final PrintCutoverService service;
    public PrintCutoverController(PrintCutoverService service){this.service=service;}
    @GetMapping public PrintApiResponse<?> status(@RequestParam String parkId,HttpServletRequest request,HttpServletResponse response) {
        response.setHeader("Cache-Control","no-store");
        return PrintTemplateController.response(service.status(parkId),request);
    }
}
