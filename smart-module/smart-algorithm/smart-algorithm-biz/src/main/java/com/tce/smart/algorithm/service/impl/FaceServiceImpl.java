package com.tce.smart.algorithm.service.impl;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.algorithm.api.dto.resp.FaceFeaturesDTO;
import com.tce.smart.algorithm.api.enums.AlgorithmTypeEnum;
import com.tce.smart.algorithm.config.properties.SeetaCompareProperties;
import com.tce.smart.algorithm.service.AlgorithmConfigService;
import com.tce.smart.algorithm.service.IFaceService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.HttpUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 人脸服务实现
 * @date: 2020-07-03 15:34
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@Service
@AllArgsConstructor
public class FaceServiceImpl implements IFaceService {

	private final AlgorithmConfigService algorithmConfigService;

	@Override
	public FaceFeaturesDTO featuresExtract(String base64Face) throws Exception {
		FaceFeaturesDTO faceFeaturesInfo = FaceFeaturesDTO.FaceFeaturesDTOBuilder.builder().build();
		String jsonBody = doRequestDeviceFacePlatform(base64Face, "v1/face/feature/extract",faceFeaturesInfo);
		JSONObject bodyObject = JSONUtil.parseObj(jsonBody);
		JSONObject data = bodyObject.getJSONObject("data");
		if(null != data){
			String faceFeature = data.getStr("faceFeature");
			faceFeaturesInfo.setFaceFeature(faceFeature);
		}
		return faceFeaturesInfo;
	}

	/**
	 * 请求设备管理平台
	 *
	 * @param obj
	 * @param route
	 * @param bridgeResult
	 * @throws Exception
	 */
	protected String doRequestDeviceFacePlatform(Object obj, String route, Result bridgeResult) throws Exception {
		Map<String, Object> req = assembleReq(obj);
		//log.info("json转换前:{}",addCardInfoReq.get("faceImage"));
		String jsonReq = JSONUtil.toJsonStr(req);
		//如果字符太长 只输出1000
		log.info("请求路径:{} 参数:{}", route, (jsonReq.length() > 1000 ? jsonReq.substring(0, 1000) : jsonReq));
		long startMill = System.currentTimeMillis();
		SeetaCompareProperties seetaCompareProperties = algorithmConfigService.getAlgorithmProperties(AlgorithmTypeEnum.FACE_FEATURES_EXTRACT.getType(), SeetaCompareProperties.class);
		HttpResponse response = HttpUtils.createPost(seetaCompareProperties.getUrl())
				.timeout(10000)
				.body(jsonReq)
				.contentType(MediaType.APPLICATION_JSON.toString())
				.execute();
		long totalMil = System.currentTimeMillis() - startMill;
		String responseBody = response.body();
		log.info("请求路径:{},耗时:{}毫秒,返回结果:{}", seetaCompareProperties.getUrl(), totalMil, responseBody);

		setBridgeResultByResp(bridgeResult, responseBody);

		return responseBody;
	}

	/**
	 * 根据设备管理端返回参数设置BridgeResult
	 *
	 * @param bridgeResult
	 * @param responseBody
	 */
	protected void setBridgeResultByResp(Result bridgeResult, String responseBody) {
		if (StringUtils.isNotBlank(responseBody)) {
			JSONObject ctrldoorResp = JSONUtil.parseObj(responseBody);
			if (null != ctrldoorResp) {
				int deviceCode = ctrldoorResp.getInt("code");
				String deviceMsg = ctrldoorResp.getStr("message");
				bridgeResult.setCode(deviceCode);
				bridgeResult.setMsg(deviceMsg);
			}
		}
	}

	protected Map<String, Object> assembleReq(Object obj) {
		if(obj instanceof String){
			return assembleQueryFeatureReq(obj);
		}else if(obj instanceof Map){
			return assembleTwoFaceCompareReq(obj);
		}
		return null;
	}

	/**
	 * 特征值参数
	 * @param obj
	 * @return
	 */
	private Map<String, Object> assembleQueryFeatureReq(Object obj){
		Map<String,Object> queryFeatureReq = new HashMap<>();
		queryFeatureReq.put("imageData",obj);
		return queryFeatureReq;
	}

	/**
	 * 比对参数
	 * @param obj
	 * @return
	 */
	private Map<String, Object> assembleTwoFaceCompareReq(Object obj){
		Map<String,String> twoFaceCompareMap = (Map) obj;
		Map<String,Object> queryFeatureReq = new HashMap<>();
		queryFeatureReq.putAll(twoFaceCompareMap);
		return queryFeatureReq;
	}
}
