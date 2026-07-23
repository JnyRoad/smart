package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.feign.msg.RemoteOaWorkFlowService;
import com.tce.smart.data.api.vo.msg.QueryOaStaffRespVo;
import com.tce.smart.platform.api.dto.resp.ReleaseStaffLookupRespDTO;
import com.tce.smart.platform.core.entity.SmtArticlesRelease;
import com.tce.smart.platform.core.mapper.SmtArticlesReleaseMapper;
import com.tce.smart.platform.service.ApproveListService;
import com.tce.smart.platform.service.impl.SmtArticlesReleaseServiceImpl;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * 办公区物品放行人员查询的最小资料和草稿归属契约。
 *
 * 人员搜索只能在当前用户持有的服务端草稿上执行，不能把 OA 原始员工对象返回给浏览器。
 */
public class SmtArticlesReleaseControllerAccessTest {

	@Test
	public void releaseStaffLookupReturnsOnlyIdAndNameForOwnedDraft() {
		SmtArticlesReleaseMapper mapper = Mockito.mock(SmtArticlesReleaseMapper.class);
		RemoteOaWorkFlowService oaWorkFlowService = Mockito.mock(RemoteOaWorkFlowService.class);
		SmtArticlesRelease release = officeDraft(17L, "owner-badge", 1);
		QueryOaStaffRespVo oaStaff = new QueryOaStaffRespVo();
		oaStaff.setID(9);
		oaStaff.setLASTNAME("测试员工");
		Mockito.when(mapper.selectById(17L)).thenReturn(release);
		Mockito.when(oaWorkFlowService.getOAInfoByBadge("A100")).thenReturn(Result.success(oaStaff));

		SmtArticlesReleaseServiceImpl service = service(mapper, oaWorkFlowService);
		ReleaseStaffLookupRespDTO result = service.lookupStaffForRelease(
				"owner-badge", Collections.singletonList(1), 17L, "A100");

		assertEquals(Integer.valueOf(9), result.getId());
		assertEquals("测试员工", result.getName());
		Set<String> fields = Arrays.stream(ReleaseStaffLookupRespDTO.class.getDeclaredFields())
				.map(Field::getName)
				.collect(Collectors.toSet());
		assertEquals(new HashSet<>(Arrays.asList("id", "name")), fields);
	}

	@Test
	public void releaseStaffLookupRejectsForeignDraft() {
		SmtArticlesReleaseMapper mapper = Mockito.mock(SmtArticlesReleaseMapper.class);
		SmtArticlesRelease release = officeDraft(17L, "owner-badge", 1);
		Mockito.when(mapper.selectById(17L)).thenReturn(release);
		SmtArticlesReleaseServiceImpl service = service(mapper, Mockito.mock(RemoteOaWorkFlowService.class));

		try {
			service.lookupStaffForRelease("other-badge", Collections.singletonList(1), 17L, "A100");
			fail("非草稿所有者不能查询放行人员");
		} catch (AccessDeniedException expected) {
			// 预期：仅数据库中登记的草稿所有者可执行查询。
		}
	}

	@Test
	public void legacyOaStaffLookupIsNotAnExternalControllerMethod() {
		try {
			SmtArticlesReleaseController.class.getMethod("getOAInfoByBadge", String.class);
			fail("旧 OA 员工查询路由不能继续暴露");
		} catch (NoSuchMethodException expected) {
			// 预期：调用方只能走带草稿归属校验的新接口。
		}
	}

	private SmtArticlesReleaseServiceImpl service(SmtArticlesReleaseMapper mapper,
			RemoteOaWorkFlowService oaWorkFlowService) {
		SmtArticlesReleaseServiceImpl service = new SmtArticlesReleaseServiceImpl();
		ReflectionTestUtils.setField(service, "baseMapper", mapper);
		ReflectionTestUtils.setField(service, "remoteOaWorkFlowService", oaWorkFlowService);
		ApproveListService approveListService = Mockito.mock(ApproveListService.class);
		Mockito.when(approveListService.list(Mockito.any())).thenReturn(Collections.emptyList());
		ReflectionTestUtils.setField(service, "approveListService", approveListService);
		return service;
	}

	private SmtArticlesRelease officeDraft(Long id, String ownerBadge, Integer parkId) {
		SmtArticlesRelease release = new SmtArticlesRelease();
		release.setId(id);
		release.setBadge(ownerBadge);
		release.setParkId(parkId);
		return release;
	}
}
