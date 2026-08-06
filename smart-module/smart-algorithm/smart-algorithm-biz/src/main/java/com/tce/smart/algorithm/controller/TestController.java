package com.tce.smart.algorithm.controller;

import com.tce.smart.algorithm.api.dto.req.CompareDTO;
import com.tce.smart.algorithm.api.dto.resp.AlgorithmConfigListDTO;
import com.tce.smart.algorithm.api.dto.resp.FaceDetectTypeDTO;
import com.tce.smart.algorithm.api.dto.resp.LivenessDTO;
import com.tce.smart.common.core.model.Result;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @ClassName: TestController
 * @Package com.tce.smart.algorithm.controller
 * @Description:
 * @Author wuxinjian
 * @Date 2020/3/16 17:55
 * @Version V1.0
 */
@Controller
@RequestMapping("/test")
@AllArgsConstructor
public class TestController {

	private final AlgorithmConfigController algorithmConfigController;

	private final FaceDetectController faceDetectController;

	private final OcrController ocrController;

	private final CompareController compareController;

	@GetMapping
	public String index () {
		return "index";
	}

	@ResponseBody
	@GetMapping("/algorithms")
	public Result<List<AlgorithmConfigListDTO>> algorithms(@RequestParam(value = "type", required = false) String type) {
		return algorithmConfigController.algorithms(type);
	}

	@ResponseBody
	@GetMapping("/face/detect/type")
	public Result<List<FaceDetectTypeDTO>> faceDetectType(){
		return faceDetectController.getFaceDetectType();
	}

	@ResponseBody
	@PostMapping("/face/detect/{algorithmType}/{faceDetectType}/{id}")
	public Result<String> faceDetect(@PathVariable("id") String id,
									 @PathVariable("algorithmType") String algorithmType,
									 @PathVariable("faceDetectType") Integer faceDetectType,
									 @RequestBody @Valid @NotBlank(message = "图片不能为空") String imageBase64){
		return faceDetectController.faceDetect(id, algorithmType, faceDetectType, imageBase64);
	}

	@ResponseBody
	@PostMapping("/ocr/{algorithmType}/{cardType}/{id}")
	public Result<String> ocr (@PathVariable("id") String id,
							   @PathVariable("algorithmType") String algorithmType,
							   @PathVariable("cardType") String cardType,
							   @RequestBody @NotBlank(message = "图片Base64不能为空") String imageBase64) {
		return ocrController.ocr(id, algorithmType, cardType, imageBase64);
	}

	@ResponseBody
	@PostMapping("/compare/{algorithmType}/{id}")
	public Result<com.tce.smart.algorithm.api.dto.resp.CompareDTO> compare(@PathVariable("id") String id,
																		   @PathVariable("algorithmType") String algorithmType,
																		   @RequestBody @Valid CompareDTO compareDTO){
		return compareController.compare(id, algorithmType, compareDTO);
	}
}
