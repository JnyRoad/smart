package com.tce.smart.algorithm.api.dto.resp;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author wuxinjian
 * @date 2020-02-07 13:05:42
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AlgorithmOcrRecordListDTO extends BaseDTO {

	/**
	 * 主键ID
	 */
	@ApiModelProperty(value = "OCR识别记录ID")
	private String id;
	/**
	 * 算法类型：文通OCR（ocr_wentong）（身份证正面/反面）；阿里OCR（ocr_ali）（身份证正面）；百度离线静默活体（staticlive_baidu_offline）；百度离线人像比对（compare_baidu_offline）；中科视拓静默活体（staticlive_seeta）；中科视拓人像比对（compare_seeta）；飞搜人像比对（compare_faceall）；
	 */
	@ApiModelProperty(value = "算法类型")
	private String algorithmType;
	/**
	 * 算法名称
	 */
	@ApiModelProperty(value = "算法名称")
	private String algorithmName;
	/**
	 * 唯一请求ID
	 */
	@ApiModelProperty(value = "请求标识")
	private String requestId;
	/**
	 * 证件类型
	 */
	@ApiModelProperty(value = "证件类型")
	private String cardType;
	/**
	 * 证件类型
	 */
	@ApiModelProperty(value = "证件类型名称")
	private String cardTypeName;
	/**
	 * 1成功,0失败
	 */
	@ApiModelProperty(value = "请求结果:1成功,0失败")
	private Integer isSuccess;
	/**
	 * 错误信息
	 */
	@ApiModelProperty(value = "错误信息")
	private String errorMessage;
	/**
	 * 请求耗时(ms)
	 */
	@ApiModelProperty(value = "请求耗时(ms)")
	private Integer consumeTime;
	/**
	 * 创建时间
	 */
	@ApiModelProperty(value = "创建时间")
	private String createTime;

}
