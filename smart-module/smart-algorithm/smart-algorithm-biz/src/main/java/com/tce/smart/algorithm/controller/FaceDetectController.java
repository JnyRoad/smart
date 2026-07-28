package com.tce.smart.algorithm.controller;

import com.tce.smart.algorithm.api.dto.resp.FaceDetectTypeDTO;
import com.tce.smart.algorithm.api.enums.FaceDetectTypeEnum;
import com.tce.smart.algorithm.service.FaceDetectStrategyService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
//import com.tce.smart.file.api.feign.RemoteFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.List;


/**
 * <p>
 * OCR识别算法控制层
 * </p>
 *
 * @author wxjason
 */
@RestController
@RequestMapping("/facedetect")
@AllArgsConstructor
@Api(value = "facedetect", tags = "人脸检测算法")
public class FaceDetectController extends BaseController {

	private final FaceDetectStrategyService faceDetectStrategyService;

	//private final RemoteFileService remoteFileService;

	@ApiOperation("人脸检测-图片Base64")
	@Inner
	@OpenApi("server")
	@PostMapping("/{algorithmType}/{faceDetectType}/{id}")
	public Result<String> faceDetect(@PathVariable("id") String id,
									 @PathVariable("algorithmType") String algorithmType,
									 @PathVariable("faceDetectType") Integer faceDetectType,
									 @RequestBody @Valid @NotBlank(message = "图片不能为空") String imageBase64){
		return success(getFaceDetect(id, algorithmType, faceDetectType, imageBase64));
	}

	@ApiOperation("人脸检测-图片ID")
	@Inner
	@OpenApi("server")
	@PostMapping("/id/{algorithmType}/{faceDetectType}/{id}")
	public Result<String> faceDetectByImageId(@PathVariable("id") String id,
									 @PathVariable("algorithmType") String algorithmType,
									 @PathVariable("faceDetectType") Integer faceDetectType,
									 @RequestParam("imageId") String imageId){
		//return success(getFaceDetect(id, algorithmType, faceDetectType, Base64Utils.encodeToString(remoteFileService.getById(imageId, SecurityConstants.FROM_IN).getData())));
		return success(getFaceDetect(id, algorithmType, faceDetectType, Base64Utils.encodeToString(null)));
	}

	/**
	 * 活体检测类型列表
	 *
	 * @return
	 */
	@ApiOperation("活体检测类型列表")
	@GetMapping("/type")
	public Result<List<FaceDetectTypeDTO>> getFaceDetectType() {
		List<FaceDetectTypeEnum> faceDetectTypeEnums = Arrays.asList(FaceDetectTypeEnum.values());
		return success(faceDetectTypeEnums, FaceDetectTypeDTO.class);
	}

	private String getFaceDetect(String id, String algorithmType, Integer faceDetectType, String imageBase64) {
		return faceDetectStrategyService.faceDetect(id, algorithmType, faceDetectType, imageBase64);
	}
}
