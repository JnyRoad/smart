package com.tce.smart.platform.controller.securityzone;

import com.tce.smart.platform.core.vo.SecurityDispatchAcceptedVO;
import com.tce.smart.platform.core.service.SmtSecurityAreaService;
import com.tce.smart.platform.service.securityzone.SmtSecurityAuthApplyService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 保密区权限下发命令端点必须只确认受理，不得把 HTTP 返回误认为设备已完成下发。
 */
public class SmtSecurityAuthApplyDispatchControllerTest {

	private SmtSecurityAuthApplyService applyService;
	private MockMvc mockMvc;

	@Before
	public void setUp() {
		applyService = mock(SmtSecurityAuthApplyService.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new SmtSecurityAuthApplyController(
				applyService, mock(SmtSecurityAreaService.class))).build();
	}

	@Test
	public void dispatch_returns202WithAcceptedBatch() throws Exception {
		when(applyService.acceptDispatch(1001L))
				.thenReturn(new SecurityDispatchAcceptedVO(9001L, 2, 0));

		mockMvc.perform(post("/security/auth/apply/1001/dispatch"))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.data.batchId").value(9001L))
				.andExpect(jsonPath("$.data.acceptedCount").value(2))
				.andExpect(jsonPath("$.data.takeoverCount").value(0));
	}
}
