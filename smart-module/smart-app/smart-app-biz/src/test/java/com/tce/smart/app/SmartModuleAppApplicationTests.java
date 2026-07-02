package com.tce.smart.app;

import com.tce.smart.app.service.AppSubjectService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@org.junit.Ignore("历史手工脚本壳：@Test 已全部注释，无可运行用例，整类忽略以避免 surefire 报 initializationError")
public class SmartModuleAppApplicationTests {

	/*@Autowired
	private AppSubjectService appSubjectService;

	@Test
	public void contextLoads() {
		appSubjectService.letTopById(1);
		appSubjectService.moveDownById(1);
	}*/

}
