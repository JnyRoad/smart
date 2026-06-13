package com.tce.smart.bridge.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.bridge.api.dto.req.BridgeDTO;
import com.tce.smart.bridge.kafka.KafkaProducer;
import com.tce.smart.bridge.service.BridgeService;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.util.HttpUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @Description: TODO
 * @ProjectName smart-bridge
 * @ClassName: BridgeServiceImpl
 * @Author jinbo
 * @Date 2019/11/6
 */
@Slf4j
@Service
public class BridgeServiceImpl implements BridgeService {
	@Value("${smart.bridge.c-process.url:http://smart-device:8106 }")
	private String url;
	@Value("${smart.bridge.c-process.bridge-url:http://smart-bridge:6062 }")
	private String urlBridge;
	@Autowired
	private KafkaProducer kafkaProducer;

	@Override
	public <T> Object dispatch(BridgeDTO<T> bridgeDTO) {
		printLog(bridgeDTO);
		Integer eventType = bridgeDTO.getEventType();
		EventEnum eventEnum = EventEnum.eventEnum(eventType);
		if (eventEnum.equals(EventEnum.UNKNOWN)) {
			throw new TCEException("未定义的事件类型：" + eventType);
		}
		if (EventEnum.isKafka(eventEnum)) {
			return kafka(eventEnum, bridgeDTO);
		}
		if (EventEnum.isGet(eventEnum)) {
			return get(eventEnum, bridgeDTO);
		}
		if (eventEnum.getCode() >= EventEnum.METER_CHECK_ONLINE.getCode()
				&& eventEnum.getCode() <= EventEnum.WATER_METER_DEL_FILE.getCode()) {
			if (EventEnum.isPost(eventEnum)) {
				return postBridge(eventEnum, bridgeDTO);
			}
		} else {
			if (EventEnum.isPost(eventEnum)) {
				return post(eventEnum, bridgeDTO);
			}
		}
		return false;
	}

	/**
	 * 该方法专门用来打印下发内容中排除图片内容 因为图片内容太多了
	 *
	 * @param bridgeDTO
	 * @param <T>
	 */
	private <T> void printLog(BridgeDTO<T> bridgeDTO) {
		String dataKey = "data";
		String imgKey = "faceImage";
		JSONObject object = JSONUtil.parseObj(bridgeDTO);
		if (null != object && object.containsKey(dataKey)) {
			try {
				JSONObject obj = object.getJSONObject(dataKey);
				if (null != obj && obj.containsKey(imgKey)) {
					//显示图片内容的前10个字符
					obj.put(imgKey, obj.getStr(imgKey).substring(0, 20));
				}
			} catch (Exception e) {
				log.info("打印信息解析失败");
			}
		}
		log.info("接收到设备连接请求，{}", object);
	}

	private <T> Object kafka(EventEnum eventEnum, BridgeDTO<T> bridgeDTO) {
		return kafkaProducer.sendMessage(bridgeDTO.getDeviceId(), eventEnum.getKey(), JSONUtil.toJsonStr(bridgeDTO.getData()));
	}

	private <T> Object post(EventEnum eventEnum, BridgeDTO<T> bridgeDTO) {
		HttpRequest httpRequest = HttpUtils.createPost(url(eventEnum)).timeout(30000).body(JSONUtil.toJsonStr(bridgeDTO.getData()));
		HttpResponse response = httpRequest.execute();
		if(null != httpRequest.getConnection()) {
			httpRequest.getConnection().setReadTimeout(30000);
		}
		if (response.isOk()) {
			return response.body();
		}
		return null;
	}

	private <T> Object postBridge(EventEnum eventEnum, BridgeDTO<T> bridgeDTO) {
		String url = urlBridge(eventEnum);
		log.info("水电集中器服务地址为：" + url);
		HttpRequest httpRequest = HttpUtils.createPost(url).timeout(30000).body(JSONUtil.toJsonStr(bridgeDTO.getData()));
		HttpResponse response = httpRequest.execute();
		if(null != httpRequest.getConnection()) {
			httpRequest.getConnection().setReadTimeout(30000);
		}
		if (response.isOk()) {
			return response.body();
		}
		return null;
	}

	private <T> Object get(EventEnum eventEnum, BridgeDTO<T> bridgeDTO) {
		HttpResponse response = HttpUtils.createGet(url(eventEnum)).body(JSONUtil.toJsonStr(bridgeDTO.getData())).execute();
		if (response.isOk()) {
			return response.body();
		}
//		HttpClient client = null;
//		HttpMethod method = null;
//		try {
//			client = new HttpClient();
//			method = new GetMethod(url(eventEnum));
//			method.setQueryString();
//			client.executeMethod(method);
//			return method.getResponseBodyAsString();
//
//		} catch (Exception e){}
//		finally {
//			if(null != method){
//				method.releaseConnection();
//			}
//		}
		return null;
	}

	private String url(EventEnum eventEnum) {
		if (StringUtils.isEmpty(url)) {
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

}
