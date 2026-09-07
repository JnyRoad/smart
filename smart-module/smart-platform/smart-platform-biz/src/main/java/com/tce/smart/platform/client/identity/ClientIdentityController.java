package com.tce.smart.platform.client.identity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/** App 登录完成后读取可信身份、用工类别、权限和岗位；此接口不接受工号参数。 */
@RestController
@RequestMapping("/api/v1")
public class ClientIdentityController {
	private final ClientIdentityService service;
	public ClientIdentityController(ClientIdentityService service) { this.service = service; }
	@GetMapping("/me") public Map<String, Object> current() { return service.response(); }
	@GetMapping("/me/apps") public java.util.List<Map<String, Object>> apps() { return service.apps(); }
}
