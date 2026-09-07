package com.tce.smart.platform.client.release;

import java.util.Collections;
import java.util.LinkedHashMap;
import org.junit.Assert;
import org.junit.Test;

/** 验证配置绑定规范化后仍能按人员目录工号找到指定审批人。 */
public class ReleaseAccessPropertiesTest {
	@Test
	public void approverLookupAcceptsSpringNormalizedConfigurationKey() {
		ReleaseAccessProperties properties = new ReleaseAccessProperties();
		properties.setApplicantApprovers(Collections.singletonMap("appemployee", "APP_SUPERVISOR"));
		Assert.assertEquals("APP_SUPERVISOR", properties.approverFor("APP_EMPLOYEE"));
	}

	@Test
	public void validationRejectsDifferentKeysThatNormalizeToTheSameApplicant() {
		ReleaseAccessProperties properties = new ReleaseAccessProperties();
		properties.setEnabled(true);
		ReleaseAccessProperties.Post post = new ReleaseAccessProperties.Post();
		post.setId("gate"); post.setName("东门"); post.setParkId(1); post.setParkName("测试园区");
		properties.setPosts(Collections.singletonList(post));
		LinkedHashMap<String, String> approvers = new LinkedHashMap<>();
		approvers.put("app-employee", "APP_SUPERVISOR");
		approvers.put("appemployee", "APP_MANAGER");
		properties.setApplicantApprovers(approvers);

		try { properties.validate(); Assert.fail("应拒绝归一化后重复的申请人配置"); }
		catch (com.tce.smart.platform.client.identity.ClientApiException expected) { Assert.assertEquals(503, expected.getStatus()); }
	}
}
