package com.tce.smart.algorithm.controller;

import com.tce.smart.algorithm.service.OcrStrategyService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.platform.api.feign.RemoteSmtImageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;


/**
 * <p>
 * OCR识别算法控制层
 * </p>
 *
 * @author wxjason
 */
@RestController
@RequestMapping("/inner/ocr")
@AllArgsConstructor
@Api(value = "inner-ocr", tags = "OCR识别算法-内部调用")
public class OcrApiController extends BaseController {

	private final OcrStrategyService ocrStrategyService;

	private final RemoteSmtImageService remoteSmtImageService;

	@Inner
	@OpenApi("server")
	@ApiOperation("OCR识别-图片Base64")
	@PostMapping("/{algorithmType}/{cardType}/{id}")
	public Result<String> ocr (@PathVariable("id") String id,
									@PathVariable("algorithmType") String algorithmType,
									@PathVariable("cardType") String cardType,
									@RequestBody @NotBlank(message = "图片Base64不能为空") String imageBase64) {
		return success(ocrDistinguish(id, algorithmType, cardType, imageBase64));
	}

	@Inner
	@OpenApi("server")
	@ApiOperation("OCR识别-图片ID")
	@PostMapping("/id/{algorithmType}/{cardType}/{id}")
	public Result<String> ocrByImageId (@PathVariable("id") String id,
										@PathVariable("algorithmType") String algorithmType,
										@PathVariable("cardType") String cardType,
										@RequestParam("imageId")  String imageId) {
		String data = remoteSmtImageService.getImageBase64ByCode(imageId, SecurityConstants.FROM_IN).getData();
		return success(ocrDistinguish(id, algorithmType, cardType, data));
	}


	private String ocrDistinguish(String id,
	                              String algorithmType,
	                              String cardType,
	                              String imageBase64) {
		return ocrStrategyService.ocr(id, algorithmType, cardType, imageBase64);
	}
}
