package com.tce.smart.algorithm.api.feign;

import com.tce.smart.algorithm.api.dto.req.CompareDTO;
import com.tce.smart.algorithm.api.dto.req.FaceImgCutReq;
import com.tce.smart.algorithm.api.dto.req.LivenessRecordAddDTO;
import com.tce.smart.algorithm.api.dto.resp.FaceFeaturesDTO;
import com.tce.smart.algorithm.api.dto.resp.LivenessDTO;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 *
 * @author wxjason
 * @date 2018/9/4
 */
@FeignClient(value = ServiceNameConstants.ALGORITHM_SERVICE)
public interface RemoteAlgorithmService {

	/**
	 * 人脸检测接口
	 * @param id 请求唯一标识
	 * @param algorithmType 算法类型,对应com.tce.smart.algorithm.api.enums.AlgorithmTypeEnum
	 * @param faceDetectType 证件类型,对应com.tce.smart.algorithm.api.enums.FaceDetectTypeEnum
	 * @param imageBase64 需要识别的证件图片base64
	 * @param from 目前固定使用：SecurityConstants.FROM_IN
	 * @return
	 */
	@PostMapping("/inner/facedetect/{algorithmType}/{faceDetectType}/{id}")
	Result<String> faceDetect(@PathVariable("id") String id,
							  @PathVariable("algorithmType") String algorithmType,
							  @PathVariable("faceDetectType") String faceDetectType,
							  @RequestBody @Valid @NotBlank(message = "图片不能为空") String imageBase64,
							  @RequestHeader(SecurityConstants.FROM) String from,
							  @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);
	/**
	 * 人脸检测接口
	 * @param id 请求唯一标识
	 * @param algorithmType 算法类型,对应com.tce.smart.algorithm.api.enums.AlgorithmTypeEnum
	 * @param faceDetectType 证件类型,对应com.tce.smart.algorithm.api.enums.FaceDetectTypeEnum
	 * @param imageId 需要识别的证件图片ID
	 * @param from 目前固定使用：SecurityConstants.FROM_IN
	 * @return
	 */
	@PostMapping("/inner/facedetect/id/{algorithmType}/{faceDetectType}/{id}")
	Result<String> faceDetectByImageId(@PathVariable("id") String id,
							  @PathVariable("algorithmType") String algorithmType,
							  @PathVariable("faceDetectType") String faceDetectType,
							  @RequestBody @Valid @NotBlank(message = "图片ID不能为空") String imageId,
							  @RequestHeader(SecurityConstants.FROM) String from,
							  @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	/**
	 * 统一OCR识别接口
	 * @param id 请求唯一标识
	 * @param algorithmType 算法类型,对应com.tce.smart.algorithm.api.enums.AlgorithmTypeEnum
	 * @param cardType 证件类型,对应com.tce.smart.algorithm.api.enums.CardTypeEnum
	 * @param imageBase64 需要识别的证件图片base64
	 * @param from 目前固定使用：SecurityConstants.FROM_IN
	 * @return
	 */
	@PostMapping("/inner/ocr/{algorithmType}/{cardType}/{id}")
	Result<String> ocr (@PathVariable("id") String id,
						@PathVariable("algorithmType") String algorithmType,
						@PathVariable("cardType") String cardType,
						@RequestBody @NotBlank(message = "图片Base64不能为空") String imageBase64,
						@RequestHeader(SecurityConstants.FROM) String from,
						@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);
	/**
	 * 统一OCR识别接口
	 * @param id 请求唯一标识
	 * @param algorithmType 算法类型,对应com.tce.smart.algorithm.api.enums.AlgorithmTypeEnum
	 * @param cardType 证件类型,对应com.tce.smart.algorithm.api.enums.CardTypeEnum
	 * @param imageId 需要识别的证件图片ID
	 * @param from 目前固定使用：SecurityConstants.FROM_IN
	 * @return
	 */
	@PostMapping("/inner/ocr/id/{algorithmType}/{cardType}/{id}")
	Result<String> ocrByImageId (@PathVariable("id") String id,
						@PathVariable("algorithmType") String algorithmType,
						@PathVariable("cardType") String cardType,
					    @RequestParam("imageId") String imageId,
						@RequestHeader(SecurityConstants.FROM) String from,
						@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	/**
	 * 活体检测算法接口
	 * @param id                单次请求唯一ID或序列号
	 * @param algorithmType     算法类型,对应com.tce.smart.yunxun.algorithm.api.enums.AlgorithmTypeEnum中的type
	 * @param imageBase64List   活体检测的图片集合,至少传入1张图片
	 * @param from              目前固定使用：SecurityConstants.FROM_IN
	 * @return
	 */
	@PostMapping("/inner/liveness/static/{algorithmType}/{id}")
	Result<LivenessDTO> livenessStatic(@PathVariable("id") String id,
									   @PathVariable("algorithmType") String algorithmType,
									   @RequestBody List<String> imageBase64List,
									   @RequestHeader(SecurityConstants.FROM) String from,
									   @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	/**
	 * 活体检测算法接口
	 * @param id                单次请求唯一ID或序列号
	 * @param algorithmType     算法类型,对应com.tce.smart.yunxun.algorithm.api.enums.AlgorithmTypeEnum中的type
	 * @param imageIdList   	活体检测的图片ID集合,至少传入1张图片
	 * @param from              目前固定使用：SecurityConstants.FROM_IN
	 * @return
	 */
	@PostMapping("/inner/liveness/static/id/{algorithmType}/{id}")
	Result<LivenessDTO> livenessStaticByImageId(@PathVariable("id") String id,
									   @PathVariable("algorithmType") String algorithmType,
									   @RequestBody List<String> imageIdList,
									   @RequestHeader(SecurityConstants.FROM) String from,
									   @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);


	/**
     * 人像比对算法接口
     * @param id            单次请求唯一ID或序列号
     * @param algorithmType 算法类型,对应com.tce.smart.yunxun.algorithm.api.enums.AlgorithmTypeEnum中的type
     * @param compareDTO    比对的两张图片参数
     * @param from          目前固定使用：SecurityConstants.FROM_IN
     * @return 相似度,0-1小数
     */
    @PostMapping("/inner/compare/{algorithmType}/{id}")
    Result<com.tce.smart.algorithm.api.dto.resp.CompareDTO> compare(@PathVariable("id") String id,
						   @PathVariable("algorithmType") String algorithmType,
						   @RequestBody @Valid CompareDTO compareDTO,
						   @RequestHeader(SecurityConstants.FROM) String from,
						   @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	/**
	 * 人像比对算法接口
	 * @param id            单次请求唯一ID或序列号
	 * @param algorithmType 算法类型,对应com.tce.smart.yunxun.algorithm.api.enums.AlgorithmTypeEnum中的type
	 * @param compareDTO    比对的两张图片参数
	 * @param from          目前固定使用：SecurityConstants.FROM_IN
	 * @return 相似度,0-1小数
	 */
	@PostMapping("/inner/compare/id/{algorithmType}/{id}")
	Result<com.tce.smart.algorithm.api.dto.resp.CompareDTO> compareByImageId(@PathVariable("id") String id,
															@PathVariable("algorithmType") String algorithmType,
															@RequestBody @Valid CompareDTO compareDTO,
															@RequestHeader(SecurityConstants.FROM) String from,
															@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	/**
	 * 活体检测记录提交
	 *
	 * @param id            单次请求唯一ID或序列号
	 * @param algorithmType 算法类型,对应com.tce.smart.yunxun.algorithm.api.enums.AlgorithmTypeEnum中的type
	 * @param livenessRecordAddDTO livenessRecordAddDTO
	 * @param from          目前固定使用：SecurityConstants.FROM_IN
	 * @return Result
	 */
	@PostMapping("/inner/liveness/record/add/{algorithmType}/{id}")
	Result<Boolean> addLivenessRecord(@PathVariable("id") String id,
									  @PathVariable("algorithmType") String algorithmType,
									  @RequestBody LivenessRecordAddDTO livenessRecordAddDTO,
									  @RequestHeader(SecurityConstants.FROM) String from,
									  @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);


	/**
	 * 获取人脸特征值
	 *
	 * @param base64Face 图片base64内容
	 * @param from          目前固定使用：SecurityConstants.FROM_IN
	 * @return Result
	 */
	@PostMapping("/faceservice/featuresExtract")
	Result<FaceFeaturesDTO> getFaceFeatures(
												  @RequestBody String  base64Face,
												  @RequestHeader(SecurityConstants.FROM) String from,
												  @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	/**
	 * 人脸图片裁剪仅供已认证的内部服务使用。
	 *
	 * @param req 待裁剪图片
	 * @param from 固定为 {@link SecurityConstants#FROM_IN}
	 * @param serviceAuth 固定为 {@link SecurityConstants#INTERNAL_SERVICE_AUTH_REQUIRED}
	 * @return 裁剪后图片
	 */
	@PostMapping("/inner/face/cut")
	Result<String> cutFace(@RequestBody FaceImgCutReq req,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);
}
