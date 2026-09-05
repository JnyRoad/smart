package com.tce.smart.platform.controller.securityzone;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityAuthDeleteLogPageQueryReqDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityAuthDeleteLogRespDTO;
import com.tce.smart.platform.service.securityzone.SmtSecurityAuthDeleteLogService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 保密区权限自动删除报表控制器的分页默认值、参数透传和权限声明测试。 */
public class SmtSecurityAuthDeleteLogControllerTest {

	private SmtSecurityAuthDeleteLogService service;
	private MockMvc mockMvc;

	@Before
	public void setUp() {
		service = mock(SmtSecurityAuthDeleteLogService.class);
		when(service.page(any(Page.class), any(SecurityAuthDeleteLogPageQueryReqDTO.class)))
				.thenReturn(new Page<SecurityAuthDeleteLogRespDTO>(1, 20));
		mockMvc = MockMvcBuilders.standaloneSetup(new SmtSecurityAuthDeleteLogController(service)).build();
	}

	/** 无分页参数时由 Controller 明确传入契约默认 current=1、size=20。 */
	@Test
	public void page_withoutPaginationParams_usesContractDefaults() throws Exception {
		mockMvc.perform(get("/security/auth/delete/log/page"))
				.andExpect(status().isOk());

		ArgumentCaptor<Page> page = ArgumentCaptor.forClass(Page.class);
		verify(service).page(page.capture(), any(SecurityAuthDeleteLogPageQueryReqDTO.class));
		assertEquals(1L, page.getValue().getCurrent());
		assertEquals(20L, page.getValue().getSize());
	}

	/** 显式分页参数应透传给 Service，避免被 Spring 默认 Page size=10 覆盖。 */
	@Test
	public void page_withPaginationParams_forwardsCurrentAndSize() throws Exception {
		mockMvc.perform(get("/security/auth/delete/log/page")
				.param("current", "3")
				.param("size", "50")
				.param("parkId", "10"))
				.andExpect(status().isOk());

		ArgumentCaptor<Page> page = ArgumentCaptor.forClass(Page.class);
		ArgumentCaptor<SecurityAuthDeleteLogPageQueryReqDTO> query =
				ArgumentCaptor.forClass(SecurityAuthDeleteLogPageQueryReqDTO.class);
		verify(service).page(page.capture(), query.capture());
		assertEquals(3L, page.getValue().getCurrent());
		assertEquals(50L, page.getValue().getSize());
		assertEquals(Integer.valueOf(10), query.getValue().getParkId());
	}

	/** 分页、导出和任务详情分别声明查询/导出权限，防止导出复用查询权限。 */
	@Test
	public void endpoints_declareIndependentPermissions() throws Exception {
		Method page = SmtSecurityAuthDeleteLogController.class.getMethod("page", long.class, long.class,
				SecurityAuthDeleteLogPageQueryReqDTO.class);
		Method export = SmtSecurityAuthDeleteLogController.class.getMethod("export",
				SecurityAuthDeleteLogPageQueryReqDTO.class, javax.servlet.http.HttpServletResponse.class);
		Method tasks = SmtSecurityAuthDeleteLogController.class.getMethod("tasks", String.class);
		assertNotNull(page.getAnnotation(PreAuthorize.class));
		assertEquals("@pms.hasPermission('platform_security_auth_delete_log_view')",
				page.getAnnotation(PreAuthorize.class).value());
		assertEquals("@pms.hasPermission('platform_security_auth_delete_log_export')",
				export.getAnnotation(PreAuthorize.class).value());
		assertEquals("@pms.hasPermission('platform_security_auth_delete_log_view')",
				tasks.getAnnotation(PreAuthorize.class).value());
	}
}
