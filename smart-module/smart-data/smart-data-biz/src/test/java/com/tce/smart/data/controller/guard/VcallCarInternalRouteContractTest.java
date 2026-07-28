package com.tce.smart.data.controller.guard;

import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.bind.annotation.GetMapping;

import java.lang.reflect.Method;

/**
 * 物流车辆预约数据属于内部调度数据，不能由外网令牌枚举。
 */
public class VcallCarInternalRouteContractTest {

	@Test
	public void pageRouteRequiresInternalServerScope() throws Exception {
		Method method = VcallCarController.class.getDeclaredMethod("getVcallCarPage",
				com.baomidou.mybatisplus.extension.plugins.pagination.Page.class);
		Assert.assertArrayEquals(new String[]{"/internal/page"}, method.getAnnotation(GetMapping.class).value());
		Assert.assertNotNull(method.getAnnotation(Inner.class));
		Assert.assertEquals("server", method.getAnnotation(OpenApi.class).value());
	}
}
