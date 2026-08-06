package com.tce.smart.algorithm.controller;

import com.tce.smart.algorithm.api.dto.resp.FaceFeaturesDTO;
import com.tce.smart.algorithm.service.IFaceService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: FaceController
 * @date: 2020-07-03 16:22
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/faceservice")
@AllArgsConstructor
@Api(value = "faceservice", tags = "人脸服务")
public class FaceController extends BaseController {

	@Autowired
	IFaceService faceService;
	/**
	 * 人脸获取特征值
	 * @param base64Face
	 * @return
	 */
	@PostMapping("/featuresExtract")
	@Inner
	@OpenApi("server")
	public Result<FaceFeaturesDTO> featuresExtract(@RequestBody String base64Face){
		FaceFeaturesDTO faceFeaturesInfo = null;
		try {
			faceFeaturesInfo = faceService.featuresExtract(base64Face);
		} catch (Exception e) {
			log.warn("{}", "获取人脸特征值异常",e);
		}
		return success(faceFeaturesInfo);
	}
}
