package com.tce.smart.app.controller.fore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tce.smart.app.ao.fore.PerfectInfoAo;
import com.tce.smart.app.api.dto.AddIdCollectDto;
import com.tce.smart.app.dto.fore.CheckPerfectCardDto;
import com.tce.smart.app.service.AppIdentityCollectService;
import com.tce.smart.app.service.fore.PerfectInfoService;
import com.tce.smart.app.vo.fore.CheckPerfectCardVo;
import com.tce.smart.app.vo.wechat.PerfectInfoVo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

import lombok.AllArgsConstructor;

/**
 * 信息完善服务控制器
 *
 * @author mingkai.wu
 * @date 2019-05-09 14:45:43
 */
@RestController
@AllArgsConstructor
@RequestMapping("/perfect")
public class PerfectInfoController extends BaseController {

	private PerfectInfoService perfectInfoService;

	@Autowired
	private AppIdentityCollectService identityCollectService;

	/**
	 * 信息完善性检查
	 * 检查人脸信息是否完整
	 *
	 * @return Result<?>
	 */
	@GetMapping("/check/face")
	public Result<Boolean> checkperfect() {
		return perfectInfoService.checkPerfectFace();
	}

	/**
	 * 身份证OCR识别
	 *
	 * @param perfectInfoAo App信息完善Ao
	 * @return Result<PerfectInfoVo>
	 */
	@PostMapping("/identification")
	public Result<PerfectInfoVo> readIdCard(@RequestBody PerfectInfoAo perfectInfoAo) {
		return success(perfectInfoService.readIdCardPhoto(perfectInfoAo));
	}

	/**
	 * 身份证OCR识别信息检查
	 *
	 * @param checkPerfectCardDto OCR识别提交检查Dto
	 * @return Result<CheckPerfectCardVo>
	 */
	@PostMapping("/check/ocr")
	public Result<CheckPerfectCardVo> readIdCard(@RequestBody CheckPerfectCardDto checkPerfectCardDto) {
		return success(perfectInfoService.checkOcrInfo(checkPerfectCardDto));
	}

	/**
	 * 身份证、人脸照片比对
	 *
	 * @param perfectInfoAo App信息完善Ao
	 * @return Result<Boolean>
	 */
	@PostMapping("/face")
	public Result<Boolean> comparePhoto(@RequestBody PerfectInfoAo perfectInfoAo) {
		return new Result<>(perfectInfoService.comparePhoto(perfectInfoAo));
	}

	/**
	 * 收集员工信息-人脸
	 * @param addIdCollectDto 人脸信息
	 * @return  Result<Boolean> true-成功，fasle-失败
	 */
	@PostMapping("/collect/face")
	public Result<Boolean> saveFaceCollect(@RequestBody AddIdCollectDto addIdCollectDto) {
		return new Result<>(identityCollectService.saveFaceCollect(addIdCollectDto));
	}
}
