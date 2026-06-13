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
public class AlgorithmFaceDetectRecordDetailDTO extends BaseDTO {

	/**
	 * 主键ID
	 */
	@ApiModelProperty("人脸检测记录ID")
	private String id;
	/**
	 * 算法类型：文通OCR（ocr_wentong）（身份证正面/反面）；阿里OCR（ocr_ali）（身份证正面）；百度离线静默活体（staticlive_baidu_offline）；百度离线人像比对（compare_baidu_offline）；中科视拓静默活体（staticlive_seeta）；中科视拓人像比对（compare_seeta）；飞搜人像比对（compare_faceall）；
	 */
	@ApiModelProperty("算法类型")
	private String algorithmType;
	/**
	 * 算法名称
	 */
	@ApiModelProperty("算法名称")
	private String algorithmName;
	/**
	 * 人脸检测类型
	 */
	@ApiModelProperty("人脸检测类型")
	private Integer faceDetectType;
	/**
	 * 人脸检测类型名称
	 */
	@ApiModelProperty("人脸检测类型名称")
	private String faceDetectTypeName;
	/**
	 * 唯一请求ID
	 */
	@ApiModelProperty("请求标识")
	private String requestId;
	/**
	 * 特区特征值的图片ID
	 */
	@ApiModelProperty("提取特征值的图片base64")
	private String requestImageBase64;
	/**
	 * 人脸数据
	 */
	@ApiModelProperty("人脸数据")
	private String faceData;
	/**
	 * 1成功,0失败
	 */
	@ApiModelProperty("比对结果:1成功,0失败")
	private Integer isSuccess;
	/**
	 * 请求耗时(ms)
	 */
	@ApiModelProperty("请求耗时(ms)")
	private Integer consumeTime;
	/**
	 * 创建时间
	 */
	@ApiModelProperty("创建时间")
	private String createTime;

}
