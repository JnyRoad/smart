package com.tce.smart.platform.controller.admittance;

import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.platform.service.admittance.AdmittancePhotoOpenService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 照片开放接口控制器层测试（MockMvc standalone）：
 * 覆盖 photoId 校验 400、缺图 404、命中 200 与 pending 透传。
 * 鉴权拦截器不在本测试范围（smart-common-security 已有 MockMvc 端到端覆盖）。
 */
public class AdmittancePhotoOpenControllerTest {

	private static final String VALID_PHOTO_ID = "eed9a5c2-2b38-4ff5-96d2-e56c237337e1";

	private AdmittancePhotoOpenService photoService;
	private OpenApiAuthenticationAdapter adapter;
	private MockMvc mockMvc;

	@Before
	public void setUp() {
		photoService = mock(AdmittancePhotoOpenService.class);
		adapter = mock(OpenApiAuthenticationAdapter.class);
		mockMvc = MockMvcBuilders
				.standaloneSetup(new AdmittancePhotoOpenController(photoService, adapter))
				.build();
	}

	/** pending：园区范围从 adapter 取并透传给 service */
	@Test
	public void pending_delegatesWithAdapterParkIds() throws Exception {
		when(adapter.appParkIds(any())).thenReturn(Arrays.asList(7, 8));
		mockMvc.perform(get("/admittance/photo/pending"))
				.andExpect(status().isOk());
		verify(photoService).listPendingPhotoIds(Arrays.asList(7, 8));
	}

	/** download：photoId 含路径穿越字符 → 400 且不触达 service */
	@Test
	public void download_invalidPhotoId_returns400() throws Exception {
		mockMvc.perform(get("/admittance/photo/download/{id}", "..%2f..%2fetc-passwd-0000000000000000"))
				.andExpect(status().isBadRequest());
		verify(photoService, never()).loadPhoto(any());
	}

	/** download：格式过短 → 400 */
	@Test
	public void download_tooShortPhotoId_returns400() throws Exception {
		mockMvc.perform(get("/admittance/photo/download/{id}", "abc123"))
				.andExpect(status().isBadRequest());
		verify(photoService, never()).loadPhoto(any());
	}

	/** download：缺图 → 404 */
	@Test
	public void download_missingImage_returns404() throws Exception {
		when(photoService.loadPhoto(VALID_PHOTO_ID)).thenReturn(null);
		mockMvc.perform(get("/admittance/photo/download/{id}", VALID_PHOTO_ID))
				.andExpect(status().isNotFound());
	}

	/** download：命中 → 200 image/png + 字节体 */
	@Test
	public void download_existingImage_returnsPngBytes() throws Exception {
		byte[] bytes = new byte[] {9, 8, 7};
		when(photoService.loadPhoto(VALID_PHOTO_ID)).thenReturn(bytes);
		mockMvc.perform(get("/admittance/photo/download/{id}", VALID_PHOTO_ID))
				.andExpect(status().isOk())
				.andExpect(content().contentType("image/png"))
				.andExpect(content().bytes(bytes));
	}
}
