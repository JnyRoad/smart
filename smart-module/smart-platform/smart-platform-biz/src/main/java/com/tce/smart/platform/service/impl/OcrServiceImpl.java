package com.tce.smart.platform.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.common.core.util.CardUtils;
import com.tce.smart.common.core.util.HttpUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.platform.core.dto.OcrIdCardDTO;
import com.tce.smart.platform.core.dto.OcrSendDTO;
import com.tce.smart.platform.service.IOcrService;
import com.tce.smart.tool.enums.ExceptionTypeEnum;
import com.tce.smart.tool.enums.IdCardImgTypeEnum;
import com.tce.smart.tool.exception.TCEException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

/**
 *
 * @ClassName AppOaManagerService.java
 * @Author mingkai.wu
 * @Date 2019-04-29 09:43
 * @Description
 */
@Slf4j
@Service
public class OcrServiceImpl implements IOcrService {

	/**
	 * Ocr服务解析成功返回码
	 */
	public final static String ORCE_SUCCESS_CODE = "2";

	@Value("${spring.ocr.username}")
	private String ocrUserName;

	@Value("${spring.ocr.url}")
	private String ocrUrl;

	@Override
	public OcrIdCardDTO readIdCardFontImg(String fontImg) {
		return processOcrService(fontImg, IdCardImgTypeEnum.IMAGE_FRONT.getCode());
	}

	@Override
	public OcrIdCardDTO readIdCardBackImg(String backImg) {
		return processOcrService(backImg, IdCardImgTypeEnum.IMAGE_BACK.getCode());
	}

	/**
	 * 解析Ocr返回字段
	 *
	 * @param ocrRespStr       Ocr返回信息
	 * @param ocrIdCardDtoParm 身份证信息Dto
	 * @return OcrIdCardDTO 身份证信息
	 */
	private OcrIdCardDTO praseOcrResp(String ocrRespStr, OcrIdCardDTO ocrIdCardDtoParm) {

		JSONObject respObject = JSONUtil.parseObj(ocrRespStr);
		JSONObject dataObject = respObject.getJSONObject("data");

		JSONObject cardsinfoObject = dataObject.getJSONObject("cardsinfo");
		JSONObject carObjcet = Objects.nonNull(cardsinfoObject) ? cardsinfoObject.getJSONObject("card") : null;
		JSONArray itemArray = Objects.nonNull(carObjcet) ? carObjcet.getJSONArray("item") : null;
		log.info("itemArray:"+itemArray);
		JSONObject itmeObject = null;
		String itemDesc = null;
		String itemContent = null;
		if (Objects.nonNull(itemArray)) {
			for (Object o : itemArray) {
				itmeObject = (JSONObject) o;
				itemDesc = itmeObject.getStr("desc");
				itemContent = itmeObject.getStr("content");
				switch (itemDesc) {
					case "姓名":
						ocrIdCardDtoParm.setName(itemContent);
						break;
					case "性别":
						ocrIdCardDtoParm.setGender(itemContent);
						break;
					case "民族":
						ocrIdCardDtoParm.setEthnicity(itemContent);
						break;
					case "出生":
						ocrIdCardDtoParm.setBirthday(itemContent);
						break;
					case "住址":
						ocrIdCardDtoParm.setAddress(itemContent);
						break;
					case "公民身份号码":
						ocrIdCardDtoParm.setIdentityCard(itemContent);
						break;
					case "签发机关":
						ocrIdCardDtoParm.setSignOrg(itemContent);
						break;
					case "签发日期":
						ocrIdCardDtoParm.setSignDate(itemContent);
						break;
					case "有效期限":
						ocrIdCardDtoParm.setValidityDate(itemContent);
						break;
					case "有效期至":
						ocrIdCardDtoParm.setValidityEndDate(itemContent);
						break;
					default:
						break;
				}
			}
		}

		return ocrIdCardDtoParm;
	}

	private OcrIdCardDTO processOcrService(String fontImg, String imgType) {
		OcrIdCardDTO ocrIdCardDTO = null;

		if (StringUtils.isNotEmpty(fontImg)) {
			// 构造解析身份证照片请求参数
			String idCardFrontReq = creatOcrReqInfo(fontImg, ocrUserName, imgType);
			// 发送Ocr服务解析身份证照片
			try {
				//String ocrFrontRespStr = new RestTemplate().postForObject(ocrUrl, idCardFrontReq, String.class);
				String	ocrFrontRespStr = HttpUtils.createPost(ocrUrl).body(idCardFrontReq).execute().body();
				if (StringUtils.isNotEmpty(ocrFrontRespStr)) {
					// 解析身份证照片信息
					ocrIdCardDTO = new OcrIdCardDTO();
					praseOcrResp(ocrFrontRespStr, ocrIdCardDTO);
				}
			} catch (Exception e) {
				// TODO: handle exception
				throw new TCEException(ExceptionTypeEnum.SERVER_ERROR.getCode(), "识别身份证信息失败");
			}

		}
		return ocrIdCardDTO;
	}

	/**
	 * 构造解析身份证照片请求参数
	 *
	 * @param imageBase64 图片base64字符串
	 * @param userName    ocr服务用户名
	 * @param photoType   身份证照片类型 2-正面 3-背面
	 * @return String
	 */
	private String creatOcrReqInfo(String imageBase64, String userName, String photoType) {
		OcrSendDTO sendOcrDto = new OcrSendDTO();
		sendOcrDto.setUsername(userName);
		sendOcrDto.setParamdata(CardUtils.setRecgnPlainParam(imageBase64, photoType, StringUtils.EMPTY, null));
		return JSONUtil.toJsonStr(sendOcrDto);
	}
}
