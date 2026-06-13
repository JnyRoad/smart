package com.tce.smart.algorithm.constant;

/**
 * @ClassName: AlgorithmTag
 * @Package com.tce.smart.yunxun.algorithm.enums
 * @Description:
 * @Author wuxinjian
 * @Date 2019-10-10 13:43
 * @Version V1.0
 */
public interface AlgorithmConstants {

    String ALGORITHM_CONFIG_PREFIX = "algorithm_configs::";

    String UNKNOWN_ALGORITHM_TYPE = "未知算法类型";

	String LIVENESS_FAILED = "活体检测失败，尽量保证拍摄画面清晰";

	String COMPARE_FAILED = "人像比对失败，尽量保证拍摄画面清晰";

	String FACE_DETECT_FAILED = "人脸检测失败，尽量保证拍摄画面清晰";

	String OCR_ALGORITHM_PREFIX = "ocr_";

	String FACE_DETECT_ALGORITHM_PREFIX = "facedetect_";

	String LIVENESS_STATIC_ALGORITHM_PREFIX = "liveness_static_";

	String COMPARE_ALGORITHM_PREFIX = "compare_";

	int FACE_DETECT_SEETA_SUCCESS_CODE = 200;

	int FACE_DETECT_FACEALL_SUCCESS_CODE = 200;

	int LIVENESS_STATIC_SEETA_SUCCESS_CODE = 0;

	int LIVENESS_STATIC_BAIDU_SUCCESS_CODE = 0;

	int COMPARE_SEETA_SUCCESS_CODE = 200;

	int COMPARE_FACEALL_SUCCESS_CODE = 200;

	int COMPARE_BAIDU_SUCCESS_CODE = 0;
}
