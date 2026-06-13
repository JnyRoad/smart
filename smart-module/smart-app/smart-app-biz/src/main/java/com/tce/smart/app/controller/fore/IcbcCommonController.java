package com.tce.smart.app.controller.fore;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tce.smart.app.service.fore.IcbcCommonService;
import com.tce.smart.common.core.wrapper.BaseController;

import lombok.AllArgsConstructor;

/**
 * 工商银行服务控制器
 *
 * @author mkwu
 * @date 2019-08-23
 */
@RestController
@AllArgsConstructor
@RequestMapping("/icbc")
public class IcbcCommonController extends BaseController {

	private final IcbcCommonService icbcCommonService;

	/**
	 * 构造e钱包html请求
	 *
	 * @return html表单信息
	 * @throws Exception
	 */
	@RequestMapping(value = "/eaccount", produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> viewModuleImage() throws Exception {
		String buildFormHtml = icbcCommonService.buildFormHtml();
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_HTML);
		headers.set("charset", "utf-8");
		return new ResponseEntity<>(buildFormHtml, headers, HttpStatus.OK);
	}

}
