package com.tce.smart.dispatcher.service.impl;

import com.tce.smart.bridge.api.feign.RemoteBridgeService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.dispatcher.service.RemoteFeignService;
import com.tce.smart.dispatcher.security.DispatcherBridgeTargetProperties;
import com.tce.smart.platform.api.dto.resp.InternalParkBridgeTargetRespDTO;
import com.tce.smart.platform.api.feign.RemoteParkInternalService;
import feign.Feign;
import feign.Request;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.Collections;
import java.util.HashMap;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicReference;

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

	/**
	 * 成功同步后一次性替换的园区 Bridge 客户端快照，避免客户端与地址分别更新时暴露旧令牌目标。
	 */
	private final AtomicReference<Map<Integer, BridgeClient>> bridgeClients =
			new AtomicReference<>(Collections.emptyMap());

	private static final long INIT_MIN_INTERVAL_MILLIS = 30_000L;

	private volatile long lastInitMillis = 0L;

	// 连接超时时间：30秒
	private static final int CONNECT_TIMEOUT = 30000;
	// 读取超时时间：60秒（考虑到ISC平台处理可能较慢）
	private static final int READ_TIMEOUT = 60000;

	@Autowired
	private RemoteParkInternalService remoteParkInternalService;

	/**
	 * 动态 Bridge Feign 也必须使用标准 OpenFeign 内部令牌拦截器，不能绕过服务令牌申请。
	 */
	@Autowired
	@Qualifier("oauth2FeignRequestInterceptor")
	private RequestInterceptor internalServiceTokenInterceptor;

	@Autowired
	private DispatcherBridgeTargetProperties bridgeTargetProperties;

	@Override
	public RemoteBridgeService getBridge(Integer parkId) {
		BridgeClient bridgeClient = getAllowedCachedBridgeClient(parkId);
		if (bridgeClient != null) {
			return bridgeClient.service;
		}
		// 未命中时按需同步一次，30秒节流：避免未配置园区导致每次调用全量重拉园区列表
		throttledInit();
		bridgeClient = getAllowedCachedBridgeClient(parkId);
		return bridgeClient == null ? null : bridgeClient.service;
	}

	/**
	 * 每次返回动态 Feign 客户端前按当前白名单复核。配置刷新与上游同步失败并发时，
	 * 仅保留仍可携带服务令牌的快照条目，避免旧地址在下一轮成功同步前继续被使用。
	 */
	private BridgeClient getAllowedCachedBridgeClient(Integer parkId) {
		BridgeClient bridgeClient = bridgeClients.get().get(parkId);
		if (bridgeClient == null) {
			return null;
		}
		if (isAllowedBridgeTarget(bridgeClient.url, bridgeTargetProperties.getBridgeTargetAllowlist())) {
			return bridgeClient;
		}
		revokeDisallowedCachedClients();
		return null;
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
	private synchronized void init() {
		long start = DateUtils.toEpochMilli();
		log.info("开始-园区信息同步...");
		Result<List<InternalParkBridgeTargetRespDTO>> result = remoteParkInternalService.getBridgeTargets(
				SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if (result == null || !result.isSuccess()) {
			revokeDisallowedCachedClients();
			if (result == null) {
				log.error("获取园区信息失败：响应为空");
				return;
			}
			log.error("获取园区信息失败：{} - {}", result.getCode(), result.getMessage());
			return;
		}
		List<InternalParkBridgeTargetRespDTO> parkList = result.getData();
		if (parkList == null) {
			// success 且 data 为空不具备权威性，不能将其误判为园区已全部撤销。
			revokeDisallowedCachedClients();
			log.error("获取园区信息失败：成功响应缺少园区目标数据，保留最近一次成功快照");
			return;
		}
		// 每轮同步固定使用一次 Nacos 刷新后的配置快照，避免构造途中白名单变化造成部分提交。
		String bridgeTargetAllowlist = bridgeTargetProperties.getBridgeTargetAllowlist();
		Map<Integer, BridgeClient> currentClients = bridgeClients.get();
		Map<Integer, BridgeClient> nextClients = new HashMap<>();
		if (CollectionUtils.isEmpty(parkList)) {
			log.warn("园区信息为空，本次同步将撤销全部 Bridge 客户端");
		} else {
			for (InternalParkBridgeTargetRespDTO park : parkList) {
				String serviceUrl = park.getBridgeUrl();
				if(StringUtils.isEmpty(serviceUrl)){
					log.warn("园区 {} 未配置 Bridge URL", park.getId());
				} else if (!isAllowedBridgeTarget(serviceUrl, bridgeTargetAllowlist)) {
					// 地址来自数据库，必须先匹配运维配置的精确 origin，避免把 Bearer 令牌发送到错误目标。
					log.error("园区 {} 的 Bridge 地址未命中服务令牌白名单，拒绝创建客户端", park.getId());
				}else {
					BridgeClient currentClient = currentClients.get(park.getId());
					if (currentClient != null && serviceUrl.equals(currentClient.url)) {
						nextClients.put(park.getId(), currentClient);
						continue;
					}
					log.info("更新园区 Bridge 客户端，园区ID：{}", park.getId());
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
					nextClients.put(park.getId(), new BridgeClient(serviceUrl, remoteBridgeService));
					log.info("创建 Bridge Feign 客户端成功，园区ID：{}，连接超时：{}ms，读取超时：{}ms",
							park.getId(), CONNECT_TIMEOUT, READ_TIMEOUT);
				}
			}
		}
		// 只在完整构造出本轮合法目标后提交，拉取或构造失败时继续使用上一次成功快照。
		bridgeClients.set(Collections.unmodifiableMap(nextClients));
		revokeDisallowedCachedClients();
		log.info("完成-园区信息同步，耗时：{}毫秒", DateUtils.toEpochMilli() - start);
	}

	/**
	 * 用 CAS 仅撤销当前白名单外的客户端，避免覆盖并发成功同步刚提交的完整快照。
	 */
	private void revokeDisallowedCachedClients() {
		String bridgeTargetAllowlist = bridgeTargetProperties.getBridgeTargetAllowlist();
		while (true) {
			Map<Integer, BridgeClient> currentClients = bridgeClients.get();
			Map<Integer, BridgeClient> allowedClients = new HashMap<>();
			for (Map.Entry<Integer, BridgeClient> entry : currentClients.entrySet()) {
				if (isAllowedBridgeTarget(entry.getValue().url, bridgeTargetAllowlist)) {
					allowedClients.put(entry.getKey(), entry.getValue());
				}
			}
			if (allowedClients.size() == currentClients.size()) {
				return;
			}
			if (bridgeClients.compareAndSet(currentClients, Collections.unmodifiableMap(allowedClients))) {
				return;
			}
		}
	}

	private static class BridgeClient {
		private final String url;
		private final RemoteBridgeService service;

		private BridgeClient(String url, RemoteBridgeService service) {
			this.url = url;
			this.service = service;
		}
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
