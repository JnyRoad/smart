package com.tce.smart.platform.controller;

import com.tce.smart.platform.service.SmtDeviceAuthorityService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 通关权限批量授权入口校验测试（MockMvc standalone）：
 * badges 来自外部输入且此前无任何上限，超过 Oracle IN 上限(1000)的请求
 * 应在 Controller 入口被 400 拒绝，绝不触达 service 层。
 * 鉴权拦截器不在本测试范围（smart-common-security 已有覆盖）。
 */
public class SmtDeviceAuthorityControllerTest {

	private SmtDeviceAuthorityService deviceAuthorityService;
	private MockMvc mockMvc;

	@Before
	public void setUp() {
		deviceAuthorityService = mock(SmtDeviceAuthorityService.class);
		mockMvc = MockMvcBuilders
				.standaloneSetup(new SmtDeviceAuthorityController(deviceAuthorityService))
				.build();
	}

	/** 拼一个 badges 为 [B1..Bn] 的批量授权请求 JSON */
	private String relationAddJson(int badgeCount) {
		String badges = IntStream.rangeClosed(1, badgeCount)
				.mapToObj(i -> "\"B" + i + "\"")
				.collect(Collectors.joining(","));
		return "{\"authId\":100,\"type\":1,\"badges\":[" + badges + "]}";
	}

	/** 超过上限：1001 个工号 → 400，且不触达 service（否则超长 IN 会 ORA-01795 整批回滚） */
	@Test
	public void relationAdd_over1000Badges_rejectedAtEntry() throws Exception {
		mockMvc.perform(post("/device/authority/relation/add")
						.contentType(MediaType.APPLICATION_JSON)
						.content(relationAddJson(1001)))
				.andExpect(status().isBadRequest());
		verify(deviceAuthorityService, never()).deviceAuthRelationAdd(any());
	}

	/** 恰好上限：1000 个工号 → 放行到 service（不能把合法的临界请求误杀） */
	@Test
	public void relationAdd_exactly1000Badges_accepted() throws Exception {
		mockMvc.perform(post("/device/authority/relation/add")
						.contentType(MediaType.APPLICATION_JSON)
						.content(relationAddJson(1000)))
				.andExpect(status().isOk());
		verify(deviceAuthorityService).deviceAuthRelationAdd(any());
	}

	/** 空工号列表：@NotEmpty 生效 → 400（此前 Controller 缺 @Valid，DTO 上的校验注解全部形同虚设） */
	@Test
	public void relationAdd_emptyBadges_rejectedAtEntry() throws Exception {
		mockMvc.perform(post("/device/authority/relation/add")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"authId\":100,\"type\":1,\"badges\":[]}"))
				.andExpect(status().isBadRequest());
		verify(deviceAuthorityService, never()).deviceAuthRelationAdd(any());
	}
}
