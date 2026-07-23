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
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
import java.net.URI;
import java.net.URISyntaxException;
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

	/**
	 * 动态 Bridge Feign 也必须使用标准 OpenFeign 内部令牌拦截器，不能绕过服务令牌申请。
	 */
	@Autowired
	@Qualifier("oauth2FeignRequestInterceptor")
	private RequestInterceptor internalServiceTokenInterceptor;

	/**
	 * 可接收服务令牌的园区 Bridge 精确地址白名单，未配置时拒绝全部动态目标。
	 */
	@Value("${security.dispatcher.bridge-target-allowlist:}")
	private String bridgeTargetAllowlist;

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
			} else if (!isAllowedBridgeTarget(serviceUrl, bridgeTargetAllowlist)) {
				// 地址来自数据库，必须先匹配运维配置的精确 origin，避免把 Bearer 令牌发送到错误目标。
				log.error("园区 {} 的 Bridge 地址未命中服务令牌白名单，拒绝创建客户端", park.getId());
			}else {
				if (!serviceUrl.equals(bridgeUrls.get(park.getId()))) {
					log.info("更新园区 Bridge 客户端，园区ID：{}，园区名称：{}", park.getId(), park.getParkName());
					HttpMessageConverter jsonConverter = new GsonHttpMessageConverter();
					ObjectFactory<HttpMessageConverters> converter = () -> new HttpMessageConverters(jsonConverter);

					// 创建带超时配置的 Request.Options
					Request.Options options = new Request.Options(CONNECT_TIMEOUT, READ_TIMEOUT);

					RemoteBridgeService remoteBridgeService = Feign.builder()
							.encoder(new SpringEncoder(converter))
							.decoder(new SpringDecoder(converter))
							.contract(new SpringMvcContract())
							.requestInterceptor(internalServiceTokenInterceptor)
							.options(options)  // 添加超时配置
							.target(RemoteBridgeService.class, serviceUrl);
					bridges.put(park.getId(), remoteBridgeService);
					bridgeUrls.put(park.getId(), serviceUrl);
					log.info("创建 Bridge Feign 客户端成功，园区ID：{}，连接超时：{}ms，读取超时：{}ms",
							park.getId(), CONNECT_TIMEOUT, READ_TIMEOUT);
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

	/**
	 * 判断数据库中的 Bridge 地址是否与运维配置的精确 scheme、host 和 port 一致。
	 *
	 * 动态 Feign 会携带服务令牌，故不接受 userinfo、query、fragment、非 HTTP(S) 协议
	 * 或未显式配置的 IP/域名，防止 DNS/拼接地址绕过白名单。
	 */
	static boolean isAllowedBridgeTarget(String serviceUrl, String allowlist) {
		String targetOrigin = normalizeOrigin(serviceUrl);
		if (targetOrigin == null || StringUtils.isEmpty(allowlist)) {
			return false;
		}
		for (String allowedTarget : allowlist.split(",")) {
			String allowedOrigin = normalizeOrigin(allowedTarget);
			if (targetOrigin.equals(allowedOrigin)) {
				return true;
			}
		}
		return false;
	}

	private static String normalizeOrigin(String value) {
		if (StringUtils.isEmpty(value)) {
			return null;
		}
		try {
			URI uri = new URI(value.trim());
			String scheme = uri.getScheme();
			String host = uri.getHost();
			if ((!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))
					|| StringUtils.isEmpty(host)
					|| uri.getRawUserInfo() != null
					|| uri.getRawQuery() != null
					|| uri.getRawFragment() != null) {
				return null;
			}
			return scheme.toLowerCase() + "://" + host.toLowerCase()
					+ (uri.getPort() < 0 ? "" : ":" + uri.getPort());
		} catch (URISyntaxException ex) {
			return null;
		}
	}
}
