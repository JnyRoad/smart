package com.tce.smart.platform.client.release;

import java.util.Collections;
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
}
