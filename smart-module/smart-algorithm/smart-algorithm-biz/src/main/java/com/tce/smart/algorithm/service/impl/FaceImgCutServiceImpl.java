package com.tce.smart.algorithm.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.algorithm.api.enums.AlgorithmTypeEnum;
import com.tce.smart.algorithm.bean.facedetect.faceall.FaceDetectReq;
import com.tce.smart.algorithm.config.properties.FaceAllFaceDetectProperties;
import com.tce.smart.algorithm.constant.AlgorithmConstants;
import com.tce.smart.algorithm.enums.AlgorithmExceptionEnum;
import com.tce.smart.algorithm.exception.AlgorithmException;
import com.tce.smart.algorithm.service.AlgorithmConfigService;
import com.tce.smart.algorithm.service.FaceImgCutService;
import com.tce.smart.common.core.constant.enums.SuccessEnum;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.HttpUtils;
import com.tce.smart.common.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;


/**
 * @ClassName: FaceDetectSeetaServiceImpl
 * @Package com.tce.smart.algorithm.service
 * @Description: 飞搜人像比对算法人脸图片剪裁
 * @Author
 * @Date 2019-10-10 10:12
 * @Version V1.0
 */
@Slf4j
@Service
@AllArgsConstructor
public class FaceImgCutServiceImpl implements FaceImgCutService {

	private static final String ALGORITHM_TYPE = AlgorithmTypeEnum.FACEDETECT_FACEALL.getType();

	private final AlgorithmConfigService algorithmConfigService;

	@Override
	public String faceDetect(String imageBase64) {
		FaceAllFaceDetectProperties properties = algorithmConfigService.getAlgorithmProperties(ALGORITHM_TYPE, FaceAllFaceDetectProperties.class);
		String url = properties.getUrl() + "/image/cut";
		FaceDetectReq req = FaceDetectReq.builder().imageData(imageBase64).build();
		String errorMessage;
		String faceData;
		try {
			String result = HttpUtils.post(url, JSONUtil.toJsonStr(req));
			JSONObject resp = JSONUtil.parseObj(result);
			validFaceDetectResult(resp);
			faceData = resp.getJSONObject("data").getStr("faceImage");
			return faceData;

		} catch (SmartException e) {
			throw e;
		} catch (Exception e) {
			errorMessage = String.format("调用人脸剪裁接口异常:%s：%s", e.getClass().getName(), e.getMessage());
			log.error(errorMessage, e);
			throw new AlgorithmException(AlgorithmExceptionEnum.FACE_DETECT_SERVER_ERROR);
		}
	}

	private void validFaceDetectResult(JSONObject resp) {
		if (Objects.isNull(resp)) {
			throw new AlgorithmException(AlgorithmExceptionEnum.FACE_DETECT_SERVER_ERROR);
		}
		int resultCode = resp.getInt("code");
		if (AlgorithmConstants.FACE_DETECT_FACEALL_SUCCESS_CODE != resultCode) {
			throw new SmartException(resp.getStr("message"));
		}
	}
}
