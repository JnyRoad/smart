package com.tce.smart.bridge.isc.service.impl;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hikvision.artemis.sdk.ArtemisHttpUtil;
import com.hikvision.artemis.sdk.config.ArtemisConfig;
import com.tce.smart.bridge.isc.api.dto.req.BridgeDTO;
import com.tce.smart.bridge.isc.entity.EventSubscribe;
import com.tce.smart.bridge.isc.service.BridgeISCService;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.util.HttpUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.dispatcher.api.dto.resp.ISCResponse;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.enums.ISCApiErrorCodeClassifier;
import com.tce.smart.dispatcher.api.enums.ISCEventTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ISC平台对接服务
 *
 * @author sunfujian
 * @since 2021-09-16 16:30
 */
@Slf4j
@Service
public class BridgeISCServiceImpl implements BridgeISCService, CommandLineRunner, DisposableBean {
	@Value("${smart.hik.host}")
	private String host;

	@Value("${smart.hik.app-key}")
	private String appKey;

	@Value("${smart.hik.app-secret}")
	private String appSecret;

	@Value("${smart.hik.callback-url}")
	private String subscribeCallbackUrl;

	@Value("${smart.hik.event-subscribe-enabled:true}")
	private Boolean eventSubscribeEnabled;

	@Value("${smart.bridge.c-process.url:http://smart-device:8106 }")
	private String url;

	@Value("${smart.bridge.c-process.bridge-url:http://smart-bridge:6062 }")
	private String urlBridge;

	/**
	 * 是否初始化配置
	 */
	public static volatile boolean INIT = false;

	/**
	 * 事件订阅状态
	 */
	public static volatile boolean SUBSCRIBE_STATUS = false;

	/**
	 * OpenAPI接口的上下文
	 */
	private static final String ARTEMIS_PATH = "/artemis";

	/**
	 * 参数提交方式
	 */
	private static final String CONTENT_TYPE = "application/json";

	/**
	 * ISC事件订阅超过该时长未收到成功回调时，认为本地订阅标志可能失真。
	 */
	private static final long EVENT_SUBSCRIBE_HEALTH_TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(10);

	/**
	 * 最近一次成功处理ISC回调的时间；订阅刚成功时也会写入，作为首个回调的观察期。
	 */
	private volatile long lastSuccessfulEventCallbackTime;

	@Override
	public <T> String dispatch(BridgeDTO<T> bridgeDTO) {
		long startTime = System.currentTimeMillis();
		printLog(bridgeDTO);
		Integer eventType = bridgeDTO.getEventType();
		EventEnum eventEnum = EventEnum.eventEnum(eventType);
		if (eventEnum.equals(EventEnum.UNKNOWN)) {
			throw new TCEException("未定义的事件类型：" + eventType);
		}
		log.info("处理事件:{}，开始时间：{}", eventEnum.getKey(), startTime);

		try {
			initConfig();
			String prefix = "ISC";
			if(!eventEnum.name().startsWith("ISC_")){
				prefix = "C++";
			}

			String result = null;
			if (eventEnum.getCode() >= EventEnum.METER_CHECK_ONLINE.getCode()
					&& eventEnum.getCode() <= EventEnum.ELE_METER_BRAKE_READ.getCode()) {
				if (EventEnum.isPost(eventEnum)) {
					result = postBridge(eventEnum, bridgeDTO);
				}
			} else {
				if (EventEnum.isPost(eventEnum)) {
					result = post(eventEnum, bridgeDTO);
					log.info("{}接口调用结果:event={},内容={}", prefix, eventEnum.getKey(), result);
				}
			}

			long endTime = System.currentTimeMillis();
			log.info("处理事件:{}完成，耗时：{}ms", eventEnum.getKey(), (endTime - startTime));
			return result;
		} catch (TCEException e) {
			long endTime = System.currentTimeMillis();
			log.error("处理事件:{}失败，耗时：{}ms，错误：{}", eventEnum.getKey(), (endTime - startTime), e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			long endTime = System.currentTimeMillis();
			log.error("处理事件:{}失败，耗时：{}ms，错误：{}", eventEnum.getKey(), (endTime - startTime), e.getMessage(), e);
			throw new TCEException("接口处理失败：" + e.getMessage());
		}
	}

	/**
	 * 应用启动之后调用
	 * @param args
	 */
	@Override
	public void run(String... args) {
		try {
			initConfig();
			if (isEventSubscribeEnabled()) {
				subscribeEvent();
			} else {
				log.info("ISC事件订阅已关闭");
			}
		} catch (Exception e) {
			log.error("订阅事件异常：", e);
		}
	}

	/**
	 * 应用关闭之前调用
	 */
	@Override
	public void destroy() {
		try {
			if (isEventSubscribeEnabled()) {
				unsubscribeEvent();
			}
		} catch (Exception e) {
			log.error("取消订阅事件异常：", e);
		}
	}

	/**
	 * 订阅事件
	 */
	private void subscribeEvent() {
		if (!isEventSubscribeEnabled()) {
			return;
		}
		if (SUBSCRIBE_STATUS) {
			return;
		}
		Integer[] eventTypes = Arrays.stream(ISCEventTypeEnum.values())
				.filter(e -> e != ISCEventTypeEnum.UNKNOWN)
				.map(ISCEventTypeEnum::getCode).toArray(Integer[]::new);
		EventSubscribe subscribe = new EventSubscribe();
		subscribe.setEventTypes(eventTypes);
		subscribe.setEventDest(subscribeCallbackUrl);
		log.info("参数：{}", JSONUtil.toJsonStr(subscribe));
		ISCResponse response = post(EventEnum.ISC_EVENT_SUBSCRIBE, JSONUtil.toJsonStr(subscribe));
		// code=0表示订阅成功，其他表示失败
		if (response != null && isIscSuccessCode(response.getCode())) {
			log.info("订阅事件成功");
			SUBSCRIBE_STATUS = true;
			refreshEventSubscribeHealthTime();
			return;
		}
		SUBSCRIBE_STATUS = false;
		log.info("订阅事件失败,{}", response);
	}

	/**
	 * 取消订阅事件
	 */
	private void unsubscribeEvent() {
		if (!isEventSubscribeEnabled()) {
			return;
		}
		Integer[] eventTypes = Arrays.stream(ISCEventTypeEnum.values())
				.filter(e -> e != ISCEventTypeEnum.UNKNOWN)
				.map(ISCEventTypeEnum::getCode).toArray(Integer[]::new);
		EventSubscribe subscribe = new EventSubscribe();
		subscribe.setEventTypes(eventTypes);
		ISCResponse response = post(EventEnum.ISC_EVENT_UNSUBSCRIBE, JSONUtil.toJsonStr(subscribe));
		// code=0表示取消订阅成功，其他表示失败
		if (response == null || !isIscSuccessCode(response.getCode())) {
			log.info("取消订阅事件失败: {}", response);
			return;
		}
		SUBSCRIBE_STATUS = false;
		log.info("取消订阅事件成功");
	}

	private void initConfig() {
		if (!INIT) {
			// artemis网关服务器ip端口
			ArtemisConfig.host = host;
			// 秘钥appKey
			ArtemisConfig.appKey = appKey;
			// 秘钥appSecret
			ArtemisConfig.appSecret = appSecret;
			INIT = true;
		}
	}

	@Override
	public ISCResponse post(EventEnum eventEnum, String data) {
		// 调用接口
		return convertToResp(eventEnum, ArtemisHttpUtil.doPostStringArtemis(getPath(eventEnum), data, null, null, CONTENT_TYPE, null));
	}

	private <T> String post(EventEnum eventEnum, BridgeDTO<T> bridgeDTO) {
		if (EventEnum.isISC(eventEnum)) {
			// 调用接口
			Map<String,String> headerMap = new HashMap<>();
			headerMap.put("tagId","frs");
			return convertResp(eventEnum,
					ArtemisHttpUtil.doPostStringArtemis(getPath(eventEnum), JSONUtil.toJsonStr(bridgeDTO.getData()),
							null, null, CONTENT_TYPE , headerMap));
		}
		HttpRequest httpRequest = HttpUtils.createPost(url(eventEnum)).timeout(30000).body(JSONUtil.toJsonStr(bridgeDTO.getData()));
		cn.hutool.http.HttpResponse response = httpRequest.execute();
		if(null != httpRequest.getConnection()) {
			httpRequest.getConnection().setReadTimeout(30000);
		}
		if (!response.isOk()) {
			// 返回null会让上游只看到"空数据失败"而丢失失败原因
			throw new TCEException("C++模块HTTP状态异常: " + response.getStatus() + ", event=" + eventEnum.getKey());
		}
		return response.body();
	}

	private <T> String postBridge(EventEnum eventEnum, BridgeDTO<T> bridgeDTO) {
		String url = urlBridge(eventEnum);
		log.info("水电集中器服务地址为：" + url);
		HttpRequest httpRequest = HttpUtils.createPost(url).timeout(30000).body(JSONUtil.toJsonStr(bridgeDTO.getData()));
		cn.hutool.http.HttpResponse response = httpRequest.execute();
		if(null != httpRequest.getConnection()) {
			httpRequest.getConnection().setReadTimeout(30000);
		}
		if (response.isOk()) {
			return response.body();
		}
		throw new TCEException("水电集中器模块HTTP状态异常: " + response.getStatus() + ", event=" + eventEnum.getKey());
	}

	private String urlBridge(EventEnum eventEnum) {
		if (StringUtils.isEmpty(urlBridge)) {
			throw new TCEException("未定义水电表Java直连模块请求URL");
		}
		String path = urlBridge;
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		if (!eventEnum.getKey().startsWith("/")) {
			path = path.concat("/");
		}
		return path.concat(eventEnum.getKey());
	}

	@Override
	public byte[] downISCImage(EventEnum eventEnum, String picUrl, String serverCode) {
		if (StrUtil.isBlank(picUrl) || StrUtil.isBlank(serverCode)) {
			log.info("图片URL或者服务器编号不能为空");
			return null;
		}
		Map<String, String> params = new HashMap<>(2);
		params.put("picUri", picUrl);
		params.put("svrIndexCode", serverCode);
		// 调用接口
		HttpResponse response = ArtemisHttpUtil.doPostStringImgArtemis(getPath(eventEnum), JSONUtil.toJsonStr(params),
				null, null, CONTENT_TYPE, null);
		if (response == null) {
			log.warn("ISC图片下载失败:event={}, response为空", eventEnum.getKey());
			return null;
		}
		int statusCode = response.getStatusLine().getStatusCode();
		if (HttpStatus.SC_OK == statusCode) {
			try {
				byte[] responseBytes = IoUtil.readBytes(response.getEntity().getContent());
				return resolveImageResponseBytes(eventEnum, responseBytes, getEntityContentType(response));
			} catch (Exception e) {
				log.error("读取ISC图片流异常：", e);
			}
		}
		logIscImageFailure(eventEnum, response, statusCode);
		return null;
	}

	ISCResponse convertToResp(EventEnum eventEnum, String response) {
		ISCResponse resp = JSONUtil.toBean(response, ISCResponse.class);
		if (resp != null && !isIscSuccessCode(resp.getCode())) {
			String userDescription = buildIscErrorMessage(eventEnum, resp);
			log.info("ISC接口请求异常:event={}, code={}, msg={}, desc={}", eventEnum.getKey(), resp.getCode(), resp.getMsg(),
					userDescription);
			resp.setMsg(userDescription);
		}
		return resp;
	}

	byte[] resolveImageResponseBytes(EventEnum eventEnum, byte[] responseBytes, String contentType) {
		if (responseBytes == null || responseBytes.length == 0) {
			return responseBytes;
		}
		if (!isJsonPayload(responseBytes, contentType)) {
			return responseBytes;
		}
		String responseBody = new String(responseBytes, StandardCharsets.UTF_8);
		ISCResponse response = null;
		try {
			response = JSONUtil.toBean(responseBody, ISCResponse.class);
		} catch (Exception e) {
			log.warn("ISC图片下载返回JSON响应解析失败:event={}, response={}", eventEnum.getKey(), responseBody, e);
			return null;
		}
		if (response == null || StrUtil.isBlank(response.getCode())) {
			log.warn("ISC图片下载返回非图片JSON响应:event={}, response={}", eventEnum.getKey(), responseBody);
			return null;
		}
		if (isIscSuccessCode(response.getCode())) {
			log.warn("ISC图片下载返回业务成功JSON响应:event={}, code={}", eventEnum.getKey(), response.getCode());
			return null;
		}
		ISCResponse classifiedResponse = convertToResp(eventEnum, responseBody);
		log.warn("ISC图片下载返回业务失败JSON响应:event={}, response={}", eventEnum.getKey(), classifiedResponse);
		return null;
	}

	String convertResp(EventEnum eventEnum, String response) {
		ISCResponse resp = JSONUtil.toBean(response, ISCResponse.class);
		if (resp == null) {
			throw new TCEException("ISC响应数据转换异常");
		}
		if (!isIscSuccessCode(resp.getCode())) {
			String userDescription = buildIscErrorMessage(eventEnum, resp);
			log.info("ISC接口请求异常:event={}, code={}, msg={}, desc={}", eventEnum.getKey(), resp.getCode(), resp.getMsg(),
					userDescription);
			Integer errorCode = parseIscErrorCode(resp.getCode());
			if (errorCode != null) {
				throw new TCEException(errorCode, "ISC接口请求异常: " + userDescription);
			}
			throw new TCEException("ISC接口请求异常: " + userDescription);
		}
		return JSONUtil.toJsonStr(resp.getData());
	}

	private String buildIscErrorMessage(EventEnum eventEnum, ISCResponse resp) {
		String userDescription = ISCApiErrorCodeClassifier.describeForUser(eventEnum, resp.getCode());
		StringBuilder message = new StringBuilder(userDescription)
				.append(" (code=")
				.append(resp.getCode());
		if (StrUtil.isNotBlank(resp.getMsg())) {
			message.append(", rawMsg=").append(resp.getMsg());
		}
		return message.append(")").toString();
	}

	private Integer parseIscErrorCode(String errorCode) {
		if (StrUtil.isBlank(errorCode) || !errorCode.trim().toLowerCase(Locale.ROOT).startsWith("0x")) {
			log.warn("ISC接口返回非十六进制错误码：{}", errorCode);
			return null;
		}
		try {
			long parsedErrorCode = Long.parseLong(errorCode.trim().substring(2), 16);
			if (parsedErrorCode > Integer.MAX_VALUE) {
				log.warn("ISC接口错误码超出系统异常码范围：{}", errorCode);
				return null;
			}
			return (int) parsedErrorCode;
		} catch (NumberFormatException e) {
			log.warn("ISC接口错误码解析失败：{}", errorCode);
			return null;
		}
	}

	private boolean isIscSuccessCode(String errorCode) {
		if (StrUtil.isBlank(errorCode)) {
			return false;
		}
		String normalizedErrorCode = errorCode.trim().toLowerCase(Locale.ROOT);
		return "0".equals(normalizedErrorCode) || "0x00000000".equals(normalizedErrorCode);
	}

	private String getEntityContentType(HttpResponse response) {
		if (response.getEntity() == null || response.getEntity().getContentType() == null) {
			return null;
		}
		return response.getEntity().getContentType().getValue();
	}

	private boolean isJsonPayload(byte[] responseBytes, String contentType) {
		if (StrUtil.isNotBlank(contentType) && contentType.toLowerCase(Locale.ROOT).contains("json")) {
			return true;
		}
		int start = 0;
		while (start < responseBytes.length && responseBytes[start] <= ' ') {
			start++;
		}
		int end = responseBytes.length - 1;
		while (end >= start && responseBytes[end] <= ' ') {
			end--;
		}
		return start <= end && responseBytes[start] == '{' && responseBytes[end] == '}';
	}

	private void logIscImageFailure(EventEnum eventEnum, HttpResponse response, int statusCode) {
		String responseBody = null;
		try {
			if (response.getEntity() != null) {
				responseBody = new String(IoUtil.readBytes(response.getEntity().getContent()), StandardCharsets.UTF_8);
			}
		} catch (Exception e) {
			log.warn("读取ISC图片下载失败响应异常:event={}, status={}", eventEnum.getKey(), statusCode, e);
		}
		if (StrUtil.isBlank(responseBody)) {
			log.warn("ISC图片下载失败:event={}, status={}", eventEnum.getKey(), statusCode);
			return;
		}
		try {
			ISCResponse errorResponse = convertToResp(eventEnum, responseBody);
			log.warn("ISC图片下载失败:event={}, status={}, response={}", eventEnum.getKey(), statusCode, errorResponse);
		} catch (Exception e) {
			log.warn("ISC图片下载失败:event={}, status={}, response={}", eventEnum.getKey(), statusCode, responseBody);
		}
	}

	private Map<String, String> getPath(EventEnum eventEnum) {
		// 设置接口的URI地址
		Map<String, String> path = new HashMap<>(1);
		path.put("https://", ARTEMIS_PATH + eventEnum.getKey());
		return path;
	}

	private String url(EventEnum eventEnum) {
		if (StrUtil.isEmpty(url)) {
			throw new TCEException("未定义C++模块请求URL");
		}
		String path = url;
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		if (!eventEnum.getKey().startsWith("/")) {
			path = path.concat("/");
		}
		return path.concat(eventEnum.getKey());
	}

	/**
	 * 该方法专门用来打印下发内容中排除图片内容 因为图片内容太多了
	 *
	 * @param bridgeDTO
	 * @param <T>
	 */
	private <T> void printLog(BridgeDTO<T> bridgeDTO) {
		String dataKey = "data";
		String imgKey = "faces";
		JSONObject object = JSONUtil.parseObj(bridgeDTO);
		if (object.containsKey(dataKey)) {
			try {
				JSONObject obj = object.getJSONObject(dataKey);
				if (null != obj && obj.containsKey(imgKey)) {
					//显示图片内容的前10个字符
					obj.put(imgKey, "省略...");
					object.put(dataKey, obj);
				}
			} catch (Exception e) {
				log.info("打印信息解析失败");
			}
		}
		Integer eventType = bridgeDTO.getEventType();
		EventEnum eventEnum = EventEnum.eventEnum(eventType);

		log.info("请求参数：event={},参数={}", eventEnum.getKey(), object);
	}

	//每2分钟执行一次
	@Scheduled(initialDelay = 1000 * 60, fixedDelay = 1000 * 60 * 2)
	public void subscribeTask() {
		if (!isEventSubscribeEnabled()) {
			return;
		}
		if (isEventSubscribeHealthExpired()) {
			log.warn("ISC事件订阅健康超时，最近成功回调时间：{}，准备重新订阅",
					lastSuccessfulEventCallbackTime);
			SUBSCRIBE_STATUS = false;
		}
		subscribeEvent();
	}

	/**
	 * 记录一条ISC事件已被成功处理，用于证明当前事件订阅链路仍然健康。
	 */
	public void recordSuccessfulEventCallback() {
		refreshEventSubscribeHealthTime();
	}

	/**
	 * 判断本地已订阅但长期未收到成功回调的假活状态。
	 */
	private boolean isEventSubscribeHealthExpired() {
		if (!SUBSCRIBE_STATUS) {
			return false;
		}
		return System.currentTimeMillis() - lastSuccessfulEventCallbackTime
				>= EVENT_SUBSCRIBE_HEALTH_TIMEOUT_MILLIS;
	}

	/**
	 * 刷新订阅健康时间，volatile写入保证巡检线程能读取到最新时间。
	 */
	private void refreshEventSubscribeHealthTime() {
		lastSuccessfulEventCallbackTime = System.currentTimeMillis();
	}

	private boolean isEventSubscribeEnabled() {
		return Boolean.TRUE.equals(eventSubscribeEnabled);
	}
}
