package com.tce.smart.algorithm.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.algorithm.api.enums.FaceTypeEnum;
import com.tce.smart.algorithm.config.properties.FaceAllCompareProperties;
import com.tce.smart.algorithm.enums.AlgorithmExceptionEnum;
import com.tce.smart.algorithm.enums.CompareFaceAllIdCardEnum;
import com.tce.smart.algorithm.exception.AlgorithmException;
import com.tce.smart.algorithm.service.AlgorithmConfigService;
import com.tce.smart.algorithm.service.ICompareService;
import com.tce.smart.common.core.constant.enums.SuccessEnum;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.util.HttpUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.algorithm.api.enums.AlgorithmTypeEnum;
import com.tce.smart.algorithm.api.dto.req.CompareDTO;
import com.tce.smart.algorithm.constant.AlgorithmConstants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import java.util.Objects;


/**
 * @ClassName: ILivenessStaticService
 * @Package com.tce.smart.algorithm.service
 * @Description: 人像比对算法
 * @Author wuxinjian
 * @Date 2019-10-10 10:12
 * @Version V1.0
 */
@Slf4j
@Service
@AllArgsConstructor
public class CompareFaceAllServiceImpl implements ICompareService {

	private static final String ALGORITHM_TYPE = AlgorithmTypeEnum.COMPARE_FACEALL.getType();

    private final AlgorithmConfigService algorithmConfigService;

    @Override
    public com.tce.smart.algorithm.api.dto.resp.CompareDTO compare(String id, CompareDTO compareDTO) {
		FaceAllCompareProperties seetaCompareProperties = algorithmConfigService.getAlgorithmProperties(ALGORITHM_TYPE, FaceAllCompareProperties.class);
		JSONObject reqJson = new JSONObject();
		JSONObject image1 = new JSONObject();
		FaceTypeEnum faceTypeEnumA = FaceTypeEnum.faceType(compareDTO.getCompareImageA().getFaceType());
		image1.put("data", compareDTO.getCompareImageA().getImageBase64());
		image1.put("idCard", FaceTypeEnum.CERT.equals(faceTypeEnumA) ? CompareFaceAllIdCardEnum.ID_CARD.getCode() : CompareFaceAllIdCardEnum.NOT_ID_CARD.getCode());
		JSONObject image2 = new JSONObject();
		FaceTypeEnum faceTypeEnumB = FaceTypeEnum.faceType(compareDTO.getCompareImageB().getFaceType());
		image2.put("data", compareDTO.getCompareImageB().getImageBase64());
		image2.put("idCard", FaceTypeEnum.CERT.equals(faceTypeEnumB) ? CompareFaceAllIdCardEnum.ID_CARD.getCode() : CompareFaceAllIdCardEnum.NOT_ID_CARD.getCode());
		reqJson.put("serialNo", id);
		reqJson.put("image1", image1);
		reqJson.put("image2", image2);
		Double similarity = null;
		byte[] bytes = Base64.decodeBase64(image1.get("data").toString());
		log.info("image1原大小={}kb", bytes.length / 1024);
		byte[] bytes1 = Base64.decodeBase64(image2.get("data").toString());
		log.info("image2原大小={}kb", bytes1.length / 1024);
			String result = HttpUtils.post(seetaCompareProperties.getUrl(), reqJson.toString());
			log.info("比对算法结果({})：{}", id, result);
			JSONObject respJson = JSONUtil.parseObj(result);
			Integer code = respJson.getInt("code");
			if (Objects.isNull(code)) {
				throw new TCEException(AlgorithmExceptionEnum.COMPARE_SERVER_ERROR.getMessage());
			}
			if (AlgorithmConstants.COMPARE_FACEALL_SUCCESS_CODE != code) {
				throw new TCEException(respJson.getStr("message"));
			}
			similarity = respJson.getJSONObject("data").getDouble("score");
			com.tce.smart.algorithm.api.dto.resp.CompareDTO compareResp = new com.tce.smart.algorithm.api.dto.resp.CompareDTO();
			compareResp.setSimilarity(similarity);
			return compareResp;
    }

	@Override
	public String handler() {
		return ALGORITHM_TYPE;
	}
}
