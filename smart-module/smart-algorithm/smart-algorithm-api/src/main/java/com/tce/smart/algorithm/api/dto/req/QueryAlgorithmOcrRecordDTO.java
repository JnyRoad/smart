package com.tce.smart.algorithm.api.dto.req;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @ClassName: QueryAlgorithmFaceDetectRecordDTO
 * @Package com.tce.smart.algorithm.api.dto.req
 * @Description:
 * @Author wuxinjian
 * @Date 2020/2/7 16:17
 * @Version V1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QueryAlgorithmOcrRecordDTO extends BaseDTO {

	/**
	 * 算法类型：文通OCR（ocr_wentong）（身份证正面/反面）；阿里OCR（ocr_ali）（身份证正面）；百度离线静默活体（staticlive_baidu_offline）；百度离线人像比对（compare_baidu_offline）；中科视拓静默活体（staticlive_seeta）；中科视拓人像比对（compare_seeta）；飞搜人像比对（compare_faceall）；
	 */
	@ApiModelProperty("算法类型")
	private String algorithmType;
	/**
	 * 唯一请求ID
	 */
	@ApiModelProperty("请求标识")
	private String requestId;
	/**
	 * 1成功,0失败
	 */
	@ApiModelProperty("请求结果:1成功,0失败")
	private Integer isSuccess;
	/**
	 * 开始时间
	 */
	@ApiModelProperty("开始时间")
	private String beginTime;
	/**
	 * 结果时间
	 */
	@ApiModelProperty("结束时间")
	private String endTime;

}
