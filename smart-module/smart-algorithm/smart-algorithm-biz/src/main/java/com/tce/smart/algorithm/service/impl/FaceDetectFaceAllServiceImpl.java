package com.tce.smart.algorithm.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.algorithm.api.enums.AlgorithmTypeEnum;
import com.tce.smart.algorithm.api.enums.FaceDetectTypeEnum;
import com.tce.smart.algorithm.bean.facedetect.faceall.FaceDetectReq;
import com.tce.smart.algorithm.config.properties.FaceAllFaceDetectProperties;
import com.tce.smart.algorithm.constant.AlgorithmConstants;
import com.tce.smart.algorithm.enums.AlgorithmExceptionEnum;
import com.tce.smart.algorithm.exception.AlgorithmException;
import com.tce.smart.algorithm.service.AlgorithmConfigService;
import com.tce.smart.algorithm.service.IFaceDetectService;
import com.tce.smart.common.core.util.HttpUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;


/**
 * @ClassName: FaceDetectSeetaServiceImpl
 * @Package com.tce.smart.algorithm.service
 * @Description: 飞搜人像比对算法
 * @Author wuxinjian
 * @Date 2019-10-10 10:12
 * @Version V1.0
 */
@Slf4j
@Service
@AllArgsConstructor
public class FaceDetectFaceAllServiceImpl implements IFaceDetectService {

	private static final String ALGORITHM_TYPE = AlgorithmTypeEnum.FACEDETECT_FACEALL.getType();

    private final AlgorithmConfigService algorithmConfigService;

	@Override
	public String faceDetect(String id, Integer faceDetectType, String imageBase64) {
		FaceAllFaceDetectProperties properties = algorithmConfigService.getAlgorithmProperties(ALGORITHM_TYPE, FaceAllFaceDetectProperties.class);
		FaceDetectTypeEnum faceDetectTypeEnum = FaceDetectTypeEnum.faceDetectType(faceDetectType);
		if (Objects.isNull(faceDetectTypeEnum)) {
			throw new AlgorithmException(AlgorithmExceptionEnum.FACE_DETECT_TYPE_ERROR);
		}
		String url = getUrl(faceDetectTypeEnum, properties);
		FaceDetectReq req = FaceDetectReq.builder().serialNo(id).imageData(imageBase64).build();
		String errorMessage;
		String faceData;
		try {
			String result = HttpUtils.post(url, JSONUtil.toJsonStr(req));
			log.info("人脸检测结果({}):{}", id, result);
			JSONObject resp = JSONUtil.parseObj(result);
			validFaceDetectResult(resp);
			switch (faceDetectTypeEnum) {
				case FACE_FEATURE:
					faceData = resp.getJSONObject("data").getStr("faceFeature");
					return faceData;
				case FACE_CUT:
					faceData = resp.getJSONObject("data").getStr("faceImage");
					return faceData;
				default:
			}
			throw new AlgorithmException(AlgorithmExceptionEnum.FACE_DETECT_TYPE_ERROR);
		} catch (AlgorithmException e) {
			throw e;
		} catch (Exception e) {
			errorMessage = String.format("调用人脸检测接口异常:%s：%s", e.getClass().getName(), e.getMessage());
			log.error(errorMessage, e);
			throw new AlgorithmException(AlgorithmExceptionEnum.FACE_DETECT_SERVER_ERROR);
		}
	}

	@Override
	public String handler() {
		return ALGORITHM_TYPE;
	}

	private void validFaceDetectResult(JSONObject resp) {
		if (Objects.isNull(resp)) {
			throw new AlgorithmException(AlgorithmExceptionEnum.FACE_DETECT_SERVER_ERROR);
		}
		int resultCode = resp.getInt("code");
		if (AlgorithmConstants.FACE_DETECT_FACEALL_SUCCESS_CODE != resultCode) {
			throw new AlgorithmException(resultCode, resp.getStr("message"));
		}
	}

	private String getUrl(FaceDetectTypeEnum faceDetectTypeEnum, FaceAllFaceDetectProperties properties) {
		String url = properties.getUrl();
		switch (faceDetectTypeEnum) {
			case FACE_FEATURE:
				return url + "/feature/extract";
			case FACE_CUT:
				return url + "/image/cut";
			default:
				throw new AlgorithmException(AlgorithmExceptionEnum.FACE_DETECT_TYPE_ERROR);
		}
	}
}
