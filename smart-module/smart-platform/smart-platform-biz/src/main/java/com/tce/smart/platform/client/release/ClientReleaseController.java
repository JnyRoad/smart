package com.tce.smart.platform.client.release;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

/** 新增的保密物品放行 API；不改动历史 /articlesrelease、OA 回调或 H5 申请接口。 */
@RestController
@RequestMapping("/api/v1/item-passes")
public class ClientReleaseController {
	private final ClientReleaseService service;
	public ClientReleaseController(ClientReleaseService service) { this.service = service; }
	@GetMapping public List<Map<String, Object>> list(@RequestParam(value = "scope", required = false) String scope,
			@RequestParam(value = "postId", required = false) String postId) { return service.list(scope, postId); }
	@GetMapping("/posts") public Map<String, Object> options() { return service.options(); }
	@GetMapping("/{id}") public Map<String, Object> detail(@PathVariable("id") String id) { return service.detail(id); }
	@PostMapping public Map<String, Object> create(@RequestBody ClientReleaseRequests.Application body,
			@RequestHeader(value = "Idempotency-Key", required = false) String key) { return service.create(body, key); }
	@PostMapping("/{id}/actions") public Map<String, Object> action(@PathVariable("id") String id,
			@RequestBody ClientReleaseRequests.Action body, @RequestHeader(value = "Idempotency-Key", required = false) String key) {
		return service.action(id, body, key);
	}
}
