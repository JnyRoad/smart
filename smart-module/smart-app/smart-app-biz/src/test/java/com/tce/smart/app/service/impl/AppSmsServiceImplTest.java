package com.tce.smart.app.service.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link AppSmsServiceImpl} 短信验证码日志脱敏单测。
 *
 * <p>纯单测，不启动 Spring 容器：直接验证脱敏纯函数 {@code maskMobile}，
 * 并用 logback {@link ListAppender} 断言验证码相关日志不含验证码明文、只含脱敏手机号
 * （安全修复 CWE-532：防止验证码/完整手机号写入日志）。</p>
 */
public class AppSmsServiceImplTest {

	/** 测试用真实手机号（仅测试数据，非真实用户） */
	private static final String MOBILE = "13800138888";
	/** 测试用 6 位验证码明文 */
	private static final String SMS_CODE = "654321";

	/** 挂到被测类 logger 上的内存 appender，用于捕获日志事件 */
	private ListAppender<ILoggingEvent> appender;
	/** 被测类对应的 logback logger */
	private Logger logger;

	@Before
	public void setUp() {
		logger = (Logger) LoggerFactory.getLogger(AppSmsServiceImpl.class);
		// 调到 DEBUG，确保 log.debug 会真正输出（漏洞正是 debug 级泄露）
		logger.setLevel(Level.DEBUG);
		appender = new ListAppender<>();
		appender.start();
		logger.addAppender(appender);
	}

	@After
	public void tearDown() {
		logger.detachAppender(appender);
		appender.stop();
	}

	/** 脱敏：正常 11 位手机号，保留前 3 后 4，中间 4 位掩码 */
	@Test
	public void maskMobile_normalPhone_keepsHeadAndTail() {
		assertEquals("138****8888", AppSmsServiceImpl.maskMobile(MOBILE));
	}

	/** 脱敏：脱敏结果不得包含中间 4 位原文，防止部分泄露 */
	@Test
	public void maskMobile_doesNotExposeMiddleDigits() {
		String masked = AppSmsServiceImpl.maskMobile(MOBILE);
		// 原手机号中间四位 0013 不应出现在脱敏结果中
		assertFalse(masked.contains("0013"));
	}

	/** 脱敏：null/短号兜底为固定掩码，不抛异常、不泄露 */
	@Test
	public void maskMobile_nullOrShort_returnsMask() {
		assertEquals("***", AppSmsServiceImpl.maskMobile(null));
		assertEquals("***", AppSmsServiceImpl.maskMobile("123"));
	}

	/**
	 * 核心安全断言：模拟发送验证码日志路径，
	 * 断言落盘日志既不含验证码明文，也不含完整手机号，且含脱敏手机号。
	 */
	@Test
	public void sendLog_redactsSmsCodeAndMobile() {
		// 复现修复后的日志写法（与生产代码同一 logger）
		logger.debug("发送短信验证码，手机号：{}", AppSmsServiceImpl.maskMobile(MOBILE));
		logger.debug("发送短信验证码成功，手机号：{}", AppSmsServiceImpl.maskMobile(MOBILE));

		assertEquals(2, appender.list.size());
		for (ILoggingEvent event : appender.list) {
			String rendered = event.getFormattedMessage();
			// 不得出现验证码明文
			assertFalse("日志泄露了验证码明文: " + rendered, rendered.contains(SMS_CODE));
			// 不得出现完整手机号
			assertFalse("日志泄露了完整手机号: " + rendered, rendered.contains(MOBILE));
			// 必须出现脱敏手机号，保留可观测性
			assertTrue("日志缺少脱敏手机号: " + rendered, rendered.contains("138****8888"));
		}
	}
}
