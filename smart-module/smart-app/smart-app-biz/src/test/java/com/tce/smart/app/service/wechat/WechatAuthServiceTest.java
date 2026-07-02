package com.tce.smart.app.service.wechat;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/12 19:00
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@org.junit.Ignore("手工联调脚本：需真实数据库/Nacos/微信环境加载完整应用上下文，禁止自动构建执行")
public class WechatAuthServiceTest {

	@Autowired
	private WechatAuthService wechatAuthService;

	@Test
	public void test1() {
		try {
			String badge = wechatAuthService.getBadge("TEC006");
			log.info("工号：" + badge);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
}