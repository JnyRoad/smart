package com.tce.smart.algorithm.controller;

import com.tce.smart.algorithm.api.dto.req.FaceImgCutReq;
import com.tce.smart.algorithm.service.FaceImgCutService;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


/**
 * <p>
 * OCR识别算法控制层
 * </p>
 *
 * @author
 */
@Slf4j
@RestController
@RequestMapping("/inner/face")
@AllArgsConstructor
@Api(value = "inner-face", tags = "人脸剪裁算法-内部调用")
public class FaceImgCutController extends BaseController {

	private final FaceImgCutService faceImgCutService;

	@ApiOperation("人脸剪裁")
	@Inner
	@OpenApi("server")
	@PostMapping("/cut")
	public Result<String> faceDetectByImageId(@RequestBody FaceImgCutReq req){
		String faceCut = "";
		try {
			faceCut = faceImgCutService.faceDetect(req.getImageData());
		} catch (Exception e) {
			throw new TCEException(e.getMessage());
		}
		return success(faceCut);
	}
}
