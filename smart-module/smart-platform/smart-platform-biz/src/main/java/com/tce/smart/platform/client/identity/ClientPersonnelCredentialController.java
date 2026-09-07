package com.tce.smart.platform.client.identity;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.annotation.Inner;
import java.util.Collections;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * App 认证链的内部人员来源查询。它不向网关暴露，也不返回员工个人资料；DHR 的具体
 * 协议由 UPMS 适配器持有，后续对接只替换该适配器。
 */
@RestController
@RequestMapping("/internal/v1/personnel")
public class ClientPersonnelCredentialController {
	private final ClientPersonnelDirectory directory;
	public ClientPersonnelCredentialController(ClientPersonnelDirectory directory) { this.directory = directory; }

	@Inner
	@GetMapping("/{staffNo}/auth-source")
	public Result<java.util.Map<String, String>> credentialSource(@PathVariable("staffNo") String staffNo) {
		return new Result<>(Collections.singletonMap("source", directory.credentialSource(staffNo)));
	}
}
