package com.tce.smart.algorithm.controller;

import com.tce.smart.algorithm.service.CompareStrategyService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.algorithm.api.dto.req.CompareDTO;
//import com.tce.smart.file.api.feign.RemoteFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


/**
 * <p>
 * OCR识别算法控制层
 * </p>
 *
 * @author wxjason
 */
@RestController
@RequestMapping("/compare")
@AllArgsConstructor
@Api(value = "compare", tags = "人像比对算法")
public class CompareController extends BaseController {

	private final CompareStrategyService compareStrategyService;

	//private final RemoteFileService remoteFileService;

	@ApiOperation("人像比对-图片Base64")
	@PostMapping("/{algorithmType}/{id}")
	public Result<com.tce.smart.algorithm.api.dto.resp.CompareDTO> compare(@PathVariable("id") String id,
																		   @PathVariable("algorithmType") String algorithmType,
																		   @RequestBody @Valid CompareDTO compareDTO){
		return success(getCompareResult(id, algorithmType, compareDTO));
	}

	@ApiOperation("人像比对-图片ID")
	@PostMapping("/id/{algorithmType}/{id}")
	public Result<com.tce.smart.algorithm.api.dto.resp.CompareDTO> compareByImageId(@PathVariable("id") String id,
																		   @PathVariable("algorithmType") String algorithmType,
																		   @RequestBody @Valid CompareDTO compareDTO){
		// compareDTO.getCompareImageA().setImageBase64(Base64Utils.encodeToString(remoteFileService.getById(compareDTO.getCompareImageA().getImageId(), SecurityConstants.FROM_IN).getData()));
		// compareDTO.getCompareImageB().setImageBase64(Base64Utils.encodeToString(remoteFileService.getById(compareDTO.getCompareImageB().getImageId(), SecurityConstants.FROM_IN).getData()));

		compareDTO.getCompareImageA().setImageBase64(Base64Utils.encodeToString(null));
		compareDTO.getCompareImageB().setImageBase64(Base64Utils.encodeToString(null));

		return success(getCompareResult(id, algorithmType, compareDTO));
	}

    private com.tce.smart.algorithm.api.dto.resp.CompareDTO getCompareResult(String id, String algorithmType, CompareDTO compareDTO) {
        return compareStrategyService.compare(id, algorithmType, compareDTO);
    }
}
