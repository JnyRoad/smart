package com.tce.smart.common.core.model;

import com.tce.smart.common.core.constant.CommonConstants;
import org.junit.Assert;
import org.junit.Test;

/**
 * Result 响应模型单测
 */
public class ResultTest {

	/**
	 * Result.fail(String) 单参数重载历史上错误复用了 SuccessEnum.FAIL 的编码(0)，
	 * 而 0 恰好与 CommonConstants.SUCCESS 相同，导致失败结果被 isSuccess() 误判为成功。
	 */
	@Test
	public void failWithMessageShouldNotBeSuccess() {
		Result<?> result = Result.fail("some error message");

		Assert.assertFalse(result.isSuccess());
		Assert.assertEquals(Integer.valueOf(CommonConstants.FAIL), result.getCode());
	}
}
