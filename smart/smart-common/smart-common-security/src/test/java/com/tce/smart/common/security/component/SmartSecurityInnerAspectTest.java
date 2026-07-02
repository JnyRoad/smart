package com.tce.smart.common.security.component;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.annotation.Inner;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * SmartSecurityInnerAspect 三态校验行为测试。
 * <p>
 * 行为矩阵：mode(OFF/AUDIT/ENFORCE) × from 头(有/无/错) × 请求上下文(有/无)。
 * 只断言对外行为（放行 or 抛 AccessDeniedException），不绑内部实现。
 */
public class SmartSecurityInnerAspectTest {

	/** proceed() 的哨兵返回值，用于断言切面确实放行到了目标方法 */
	private static final Object PROCEED_RESULT = new Object();

	private ProceedingJoinPoint joinPoint;

	@Before
	public void setUp() throws Throwable {
		joinPoint = Mockito.mock(ProceedingJoinPoint.class);
		Mockito.when(joinPoint.proceed()).thenReturn(PROCEED_RESULT);
		// 审计/拒绝日志会读方法签名，给个真实短名避免 NPE
		Signature signature = Mockito.mock(Signature.class);
		Mockito.when(signature.toShortString()).thenReturn("DemoController.demo()");
		Mockito.when(joinPoint.getSignature()).thenReturn(signature);
	}

	@After
	public void tearDown() {
		// 清理线程绑定的请求上下文，避免用例间互相污染
		RequestContextHolder.resetRequestAttributes();
	}

	// ---------- ENFORCE：拦截模式 ----------

	@Test
	public void enforceWithFromInHeaderProceeds() throws Throwable {
		bindRequestWithFrom(SecurityConstants.FROM_IN);
		Object result = aspect(InnerMode.ENFORCE).around(joinPoint, innerAnnotation());
		Assert.assertSame(PROCEED_RESULT, result);
	}

	@Test
	public void enforceWithoutFromHeaderDenies() throws Throwable {
		bindRequestWithFrom(null);
		assertDenied(InnerMode.ENFORCE);
	}

	@Test
	public void enforceWithWrongFromHeaderDenies() throws Throwable {
		bindRequestWithFrom("N");
		assertDenied(InnerMode.ENFORCE);
	}

	@Test
	public void enforceWithoutRequestContextDenies() throws Throwable {
		// 不绑定任何请求上下文（模拟异步线程/内部误调）
		assertDenied(InnerMode.ENFORCE);
	}

	// ---------- AUDIT：审计模式（只记录，绝不打断） ----------

	@Test
	public void auditWithoutFromHeaderProceeds() throws Throwable {
		bindRequestWithFrom(null);
		Object result = aspect(InnerMode.AUDIT).around(joinPoint, innerAnnotation());
		Assert.assertSame(PROCEED_RESULT, result);
	}

	@Test
	public void auditWithFromInHeaderProceeds() throws Throwable {
		bindRequestWithFrom(SecurityConstants.FROM_IN);
		Object result = aspect(InnerMode.AUDIT).around(joinPoint, innerAnnotation());
		Assert.assertSame(PROCEED_RESULT, result);
	}

	@Test
	public void auditWithoutRequestContextProceeds() throws Throwable {
		// 审计模式的承诺是零中断：即使无请求上下文也放行
		Object result = aspect(InnerMode.AUDIT).around(joinPoint, innerAnnotation());
		Assert.assertSame(PROCEED_RESULT, result);
	}

	// ---------- OFF：关闭模式（等价历史注释态，紧急回滚兜底） ----------

	@Test
	public void offWithoutFromHeaderProceeds() throws Throwable {
		bindRequestWithFrom(null);
		Object result = aspect(InnerMode.OFF).around(joinPoint, innerAnnotation());
		Assert.assertSame(PROCEED_RESULT, result);
	}

	// ---------- @Inner(false)：注解声明不走 AOP 校验 ----------

	@Test
	public void innerValueFalseSkipsCheckEvenInEnforce() throws Throwable {
		bindRequestWithFrom(null);
		Object result = aspect(InnerMode.ENFORCE).around(joinPoint, innerFalseAnnotation());
		Assert.assertSame(PROCEED_RESULT, result);
	}

	// ---------- 默认配置 ----------

	@Test
	public void defaultModeIsAudit() {
		// 漏配配置时必须落在零中断的审计模式，绝不能默认拦截
		Assert.assertEquals(InnerMode.AUDIT, new SmartInnerSecurityProperties().getMode());
	}

	// ---------- 辅助方法 ----------

	/** 构造指定模式的切面实例 */
	private SmartSecurityInnerAspect aspect(InnerMode mode) {
		SmartInnerSecurityProperties properties = new SmartInnerSecurityProperties();
		properties.setMode(mode);
		return new SmartSecurityInnerAspect(properties);
	}

	/** 绑定一个带指定 from 头的请求上下文；from 为 null 表示不带该头 */
	private void bindRequestWithFrom(String from) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		if (from != null) {
			request.addHeader(SecurityConstants.FROM, from);
		}
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
	}

	/** 断言指定模式下被拒绝，且目标方法未被执行 */
	private void assertDenied(InnerMode mode) throws Throwable {
		try {
			aspect(mode).around(joinPoint, innerAnnotation());
			Assert.fail("应抛出 AccessDeniedException");
		} catch (AccessDeniedException expected) {
			// 预期行为：拒绝访问，且未放行到目标方法
			Mockito.verify(joinPoint, Mockito.never()).proceed();
		}
	}

	/** 通过真实注解声明获取 @Inner 实例（默认 value=true），不 mock 注解 */
	private Inner innerAnnotation() throws NoSuchMethodException {
		return Fixtures.class.getMethod("innerDefault").getAnnotation(Inner.class);
	}

	/** 获取 @Inner(false) 实例 */
	private Inner innerFalseAnnotation() throws NoSuchMethodException {
		return Fixtures.class.getMethod("innerFalse").getAnnotation(Inner.class);
	}

	/** 仅用于携带真实 @Inner 注解的测试夹具 */
	public static class Fixtures {
		@Inner
		public void innerDefault() {
		}

		@Inner(false)
		public void innerFalse() {
		}
	}
}
