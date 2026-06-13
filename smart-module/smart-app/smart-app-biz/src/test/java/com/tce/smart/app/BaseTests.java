package com.tce.smart.app;

import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @ClassName BaseTests
 * @Author: WangJinbo
 * @Date: 2018/11/6 09:23
 * @Description:
 **/
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {SmartAppApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseTests {

}
