package com.tce.smart.platform.conf;

import com.tce.smart.platform.service.SmtParkingCorrectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description: InitConfiguration
 * @date: 2020/12/8 0008 19:23
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@Component
public class InitConfiguration implements CommandLineRunner {

	@Resource
	private SmtParkingCorrectionService smtParkingCorrectionService;

	@Override
	public void run(String... args) throws Exception {
		//初始化车位检测
		try {
			log.info("初始化车位检测......");
			smtParkingCorrectionService.initParkingCorrection();
		}catch (Exception e){
			log.error(e.getMessage(), e);
		}
	}
}
