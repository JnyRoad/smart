package com.tce.smart.algorithm.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ProjectName smart-platform
 * @ClassName: DeleteEnum
 * @Author jinbo
 * @Date 2019/6/21
 */
@Getter
@AllArgsConstructor
public enum AlgorithmExceptionEnum {
	/**
	 * 成功
	 */
	OK(1, "成功"),
	OCR_FAILED(21000, "证件识别失败"),
	OCR_SERVER_ERROR(21001, "OCR识别服务异常"),
	OCR_CARD_TYPE_ERROR(21002, "证件类型有误"),
	OCR_CARD_TYPE_NOT_SUPPORTED(21003, "暂不支持该证件类型"),
	LIVENESS_STATIC_FAILED(41000, "活体检测失败"),
	LIVENESS_SERVER_ERROR(41001, "活体检测服务异常"),
	COMPARE_FAILED(51000, "人像比对失败"),
	COMPARE_SERVER_ERROR(51001, "人像比对服务异常"),
	FACE_DETECT_FAILED(61000, "人脸检测失败"),
	FACE_DETECT_SERVER_ERROR(61001, "人脸检测服务异常"),
	FACE_DETECT_TYPE_ERROR(61002, "人脸检测类型错误");

    private Integer code;

    private String message;

}
