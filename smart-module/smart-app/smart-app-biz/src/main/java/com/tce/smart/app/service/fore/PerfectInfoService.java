package com.tce.smart.app.service.fore;

import com.tce.smart.app.ao.fore.PerfectInfoAo;
import com.tce.smart.app.dto.fore.CheckPerfectCardDto;
import com.tce.smart.app.vo.fore.CheckPerfectCardVo;
import com.tce.smart.app.vo.wechat.PerfectInfoVo;
import com.tce.smart.common.core.model.Result;

/**
 * Ocr识别服务接口
 *
 * @author mingkai.wu
 * @date 2019-05-10 16:15:57
 */
public interface PerfectInfoService {

	/**
	 * 检查员工信息是否完整(人脸信息)
	 *
	 * @return
	 */
	Result<Boolean> checkPerfectFace();

	/**
	 * 利用Ocr算法读取身份证信息
	 *
	 * @param ocrAo Ocr识别Ao
	 * @return PerfectInfoVo 身份证解析信息
	 */
	PerfectInfoVo readIdCardPhoto(PerfectInfoAo ocrAo);

	/**
	 * OCR识别信息检查
	 *
	 * @param checkPerfectCardDto 身份证识别结果
	 * @return CheckPerfectCardVo 信息完善-身份证OCR识别结果校验Vo
	 */
	CheckPerfectCardVo checkOcrInfo(CheckPerfectCardDto checkPerfectCardDto);

	/**
	 * 身份证、人脸照片比对
	 *
	 * @param ocrAo Ocr识别Ao
	 * @return Boolean true-比对成功 false-比对失败
	 */
	Boolean comparePhoto(PerfectInfoAo ocrAo);

}
