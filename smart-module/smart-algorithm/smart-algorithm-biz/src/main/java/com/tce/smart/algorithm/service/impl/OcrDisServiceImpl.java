package com.tce.smart.algorithm.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.algorithm.api.enums.CardTypeEnum;
import com.tce.smart.algorithm.api.dto.resp.ItemDTO;
import com.tce.smart.algorithm.config.properties.WenTongOcrProperties;
import com.tce.smart.algorithm.constant.WenTongOcrConstants;
import com.tce.smart.algorithm.enums.AlgorithmExceptionEnum;
import com.tce.smart.algorithm.exception.AlgorithmException;
import com.tce.smart.algorithm.service.AlgorithmConfigService;
import com.tce.smart.algorithm.service.IOcrService;
import com.tce.smart.algorithm.util.WenTongOcrUtils;
import com.tce.smart.common.core.constant.NumberConstants;
import com.tce.smart.common.core.constant.enums.SuccessEnum;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.FileUtils;
import com.tce.smart.common.core.util.HttpUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.algorithm.api.enums.AlgorithmTypeEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @ClassName: WenTongWenTongOcrServiceImpl
 * @Package com.tce.smart.algorithm.service.impl
 * @Description: Ocr识别算法
 * @Author wuxinjian
 * @Date 2019-10-10 10:13
 * @Version V1.0
 */
@Slf4j
@Service
@AllArgsConstructor
public class OcrDisServiceImpl implements IOcrService {

	private static final String ALGORITHM_TYPE = AlgorithmTypeEnum.OCR_WENTONG.getType();

	private final AlgorithmConfigService algorithmConfigService;

	@Override
	public String ocr(String id, String cardType, String imageBase64) {
		CardTypeEnum cardTypeEnum = CardTypeEnum.cardType(cardType);
		int isSuccess = SuccessEnum.SUCCESS.getCode();
		String errorMessage = StringUtils.EMPTY;
		long startTime = System.currentTimeMillis();
		String handleImageBase64 = StringUtils.EMPTY;
		String headImageBase64 = StringUtils.EMPTY;
		String distinguishWords = StringUtils.EMPTY;
		try {
			if (CardTypeEnum.UNKNOWN.equals(cardTypeEnum)) {
				throw new AlgorithmException(AlgorithmExceptionEnum.OCR_CARD_TYPE_ERROR);
			}
			String result = this.distinguish(cardType, imageBase64);
			log.info("OCR识别结果({})：{}", id, result);
			List<ItemDTO> itemDTOList = this.resultToItemList(result);
			String returned = JSONUtil.toJsonStr(WenTongOcrUtils.convertToBean(itemDTOList, cardTypeEnum.getClassType()));
			Iterator<ItemDTO> it = itemDTOList.iterator();
			while (it.hasNext()) {
				ItemDTO itemDTO = it.next();
				if (WenTongOcrConstants.HANDLE_IMAGE_DESC.equals(itemDTO.getDesc())) {
					handleImageBase64 = itemDTO.getContent();
					it.remove();
				}
				if (WenTongOcrConstants.HEAD_IMAGE_DESC.equals(itemDTO.getDesc())) {
					headImageBase64 = itemDTO.getContent();
					it.remove();
				}
				if (WenTongOcrConstants.RETAIN_DESC.equals(itemDTO.getDesc())) {
					it.remove();
				}
			}
			distinguishWords = JSONUtil.toJsonStr(itemDTOList);
			return returned;
		} catch (AlgorithmException e) {
			errorMessage = e.getMessage();
			log.error(errorMessage, e);
			isSuccess = SuccessEnum.FAIL.getCode();
			throw new AlgorithmException(AlgorithmExceptionEnum.OCR_FAILED);
		} catch (Exception e) {
			errorMessage = String.format("调用OCR识别接口异常:%s：%s", e.getClass().getName(), e.getMessage());
			log.error(errorMessage, e);
			isSuccess = SuccessEnum.FAIL.getCode();
			throw new AlgorithmException(AlgorithmExceptionEnum.OCR_FAILED);
		}

    }

	@Override
	public String handler() {
		return ALGORITHM_TYPE;
	}

	/**
	 * OCR识别
	 *
	 * @param type        文通算法提供的识别文件的类型
	 * @param imageBase64 待识别的图片base64
	 * @return
	 */
	private String distinguish(String type, String imageBase64) {
		WenTongOcrProperties wenTongOcrProperties = algorithmConfigService.getAlgorithmProperties(ALGORITHM_TYPE, WenTongOcrProperties.class);
		JSONObject reqJson = new JSONObject();
		// 与文通服务端配置username一致
		reqJson.put("username", wenTongOcrProperties.getUsername());
		// 根据文通提供paramdata生成方法
		reqJson.put("paramdata", WenTongOcrUtils.setRecgnPlainParam(imageBase64, type, StringUtils.EMPTY, null));
		// 固定值：NULL
		reqJson.put("signdata", "NULL");
		// 图片类型：此处均采用jpg
		reqJson.put("imgtype", FileUtils.IMAGE_TYPE_JPG);
		return HttpUtils.post(wenTongOcrProperties.getUrl(), reqJson.toString());
	}


	/**
	 * 在识别结果中提取识别信息
	 *
	 * @param result 文通算法提供的识别文件的类型
	 * @return
	 */
	private List<ItemDTO> resultToItemList(String result) {
		JSONObject respJson = JSONUtil.parseObj(result);
		JSONObject data = respJson.getJSONObject("data");
		if (Objects.isNull(data)) {
			throw new AlgorithmException(AlgorithmExceptionEnum.OCR_FAILED);
		}
		JSONObject message = data.getJSONObject("message");
		if (Objects.isNull(message)) {
			throw new AlgorithmException(AlgorithmExceptionEnum.OCR_FAILED);
		}
		Integer status = message.getInt("status");
		if (Objects.isNull(status)) {
			throw new AlgorithmException(AlgorithmExceptionEnum.OCR_FAILED);
		}
		if (status < NumberConstants.ZERO) {
			throw new AlgorithmException(AlgorithmExceptionEnum.OCR_FAILED);
		}
		JSONObject cardsinfo = data.getJSONObject("cardsinfo");
		if (Objects.isNull(cardsinfo)) {
			throw new AlgorithmException(AlgorithmExceptionEnum.OCR_FAILED);
		}
		JSONObject card = cardsinfo.getJSONObject("card");
		if (Objects.isNull(card)) {
			throw new AlgorithmException(AlgorithmExceptionEnum.OCR_FAILED);
		}
		JSONArray itemList = card.getJSONArray("item");
		if (CollectionUtils.isEmpty(itemList)) {
			throw new AlgorithmException(AlgorithmExceptionEnum.OCR_FAILED);
		}
		return JSONUtil.toList(itemList, ItemDTO.class);
	}

}
