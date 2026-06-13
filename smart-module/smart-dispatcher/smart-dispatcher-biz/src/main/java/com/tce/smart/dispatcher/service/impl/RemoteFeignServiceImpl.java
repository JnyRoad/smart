package com.tce.smart.dispatcher.service.impl;

import com.tce.smart.bridge.api.feign.RemoteBridgeService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.dispatcher.service.RemoteFeignService;
import com.tce.smart.platform.api.dto.SmtParkDTO;
import com.tce.smart.platform.api.feign.RemoteParkService;
import feign.Feign;
import feign.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @Description: TODO
 * @ProjectName smart-dispatcher
 * @ClassName: RemoteFeignServiceImpl
 * @Author jinbo
 * @Date 2019/11/6
 */
@Slf4j
@Service
@Import(FeignClientsConfiguration.class)
public class RemoteFeignServiceImpl implements RemoteFeignService {

	private final Map<Integer, RemoteBridgeService> bridges = new ConcurrentHashMap<>();

	private final Map<Integer, String> bridgeUrls = new ConcurrentHashMap<>();

	private static final long INIT_MIN_INTERVAL_MILLIS = 30_000L;

	private volatile long lastInitMillis = 0L;

	// 连接超时时间：30秒
	private static final int CONNECT_TIMEOUT = 30000;
	// 读取超时时间：60秒（考虑到ISC平台处理可能较慢）
	private static final int READ_TIMEOUT = 60000;

	@Autowired
	private RemoteParkService remoteParkService;

	@Override
	public RemoteBridgeService getBridge(Integer parkId) {
		RemoteBridgeService bridge = bridges.get(parkId);
		if (bridge != null) {
			return bridge;
		}
		// 未命中时按需同步一次，30秒节流：避免未配置园区导致每次调用全量重拉园区列表
		throttledInit();
		return bridges.get(parkId);
	}

	private synchronized void throttledInit() {
		long now = System.currentTimeMillis();
		if (now - lastInitMillis < INIT_MIN_INTERVAL_MILLIS) {
			return;
		}
		lastInitMillis = now;
		init();
	}

	/**
	 * 初始化/同步园区信息，并构造 RemoteBridgeService
	 */
	private void init() {
		long start = DateUtils.toEpochMilli();
		log.info("开始-园区信息同步...");
		Result<List<SmtParkDTO>> result = remoteParkService.getParkList(SecurityConstants.FROM_IN);
		if (!result.isSuccess()) {
			log.error("获取园区信息失败：{} - {}", result.getCode(), result.getMessage());
			return;
		}
		List<SmtParkDTO> parkList = result.getData();
		if (CollectionUtils.isEmpty(parkList)) {
			log.warn("园区信息为空!");
			return;
		}
		parkList.forEach(park -> {
			String serviceUrl = park.getBridgeUrl();
			if(StringUtils.isEmpty(serviceUrl)){
				log.warn("园区 {} 未配置URL", park.getParkName());
			}else {
				if (!serviceUrl.equals(bridgeUrls.get(park.getId()))) {
					log.info("更新园区信息：{} - {} - {}", park.getId(), park.getParkName(), park.getBridgeUrl());
					HttpMessageConverter jsonConverter = new GsonHttpMessageConverter();
					ObjectFactory<HttpMessageConverters> converter = () -> new HttpMessageConverters(jsonConverter);

					// 创建带超时配置的 Request.Options
					Request.Options options = new Request.Options(CONNECT_TIMEOUT, READ_TIMEOUT);

					RemoteBridgeService remoteBridgeService = Feign.builder()
							.encoder(new SpringEncoder(converter))
							.decoder(new SpringDecoder(converter))
							.contract(new SpringMvcContract())
							.options(options)  // 添加超时配置
							.target(RemoteBridgeService.class, serviceUrl);
					bridges.put(park.getId(), remoteBridgeService);
					bridgeUrls.put(park.getId(), serviceUrl);
					log.info("创建Feign客户端成功，园区ID：{}，URL：{}，连接超时：{}ms，读取超时：{}ms",
							park.getId(), serviceUrl, CONNECT_TIMEOUT, READ_TIMEOUT);
				}
			}
		});
		log.info("完成-园区信息同步，耗时：{}毫秒", DateUtils.toEpochMilli() - start);
	}

	//每2分钟执行一次
	@Scheduled(initialDelay = 1000 * 60 * 2, fixedDelay = 1000 * 60 * 2)
	public void sync() {
		init();
	}
}