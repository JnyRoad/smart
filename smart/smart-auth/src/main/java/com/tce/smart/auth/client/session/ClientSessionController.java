package com.tce.smart.auth.client.session;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/** 新增 App 会话端点；既有 /oauth/token 与移动端登录接口完全保持原契约。 */
@RestController
@RequestMapping("/api/v1")
@ConditionalOnProperty(prefix = "smart.client.session", name = "enabled", havingValue = "true")
public class ClientSessionController {
	private final ClientSessionService service;
	public ClientSessionController(ClientSessionService service) { this.service = service; }

	@PostMapping("/sessions")
	public Map<String, Object> login(@RequestBody ClientSessionRequests.Login request) {
		return service.login(request.staffNo, request.password);
	}
}
