package com.tce.smart.platform.controller.admittance;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.admittance.SaveAdmittanceCarApplyReqDTO;
import com.tce.smart.platform.api.dto.req.admittance.VisitorTruckSmsVerifyReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorTruckProofRespDTO;
import com.tce.smart.platform.service.admittance.VisitorTruckProofService;
import com.tce.smart.tool.enums.AdmittanceCarCauseEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 匿名货车预约入口。
 *
 * 三个端点都必须通过本地 Nacos 精确放行；除短信核验外，后续读取和提交均需要短时 proof。
 */
@RestController
@Api(tags = "platform-货车预约")
@RequiredArgsConstructor
@RequestMapping("/admittance/visitor-truck")
public class VisitorTruckController extends BaseController {
	private static final String PROOF_HEADER = "X-Visitor-Truck-Sms-Proof";

	private final VisitorTruckProofService visitorTruckProofService;

	@ApiOperation("货车预约短信本人核验")
	@PostMapping("/verify-sms")
	public Result<VisitorTruckProofRespDTO> verifySms(@RequestBody VisitorTruckSmsVerifyReqDTO request,
			HttpServletResponse response) {
		// proof 是短时凭证，不能被浏览器或共享代理缓存。
		response.setHeader("Cache-Control", "no-store");
		return success(visitorTruckProofService.verifySms(request));
	}

	@ApiOperation("货车预约事由选项")
	@GetMapping("/options/cause")
	public Result<List<Map<String, Object>>> causeOptions(
			@RequestHeader(value = PROOF_HEADER, required = false) String proofToken) {
		visitorTruckProofService.assertActiveProof(proofToken);
		return success(AdmittanceCarCauseEnum.getTypeList());
	}

	@ApiOperation("提交货车预约")
	@PostMapping("/apply")
	public Result<Boolean> apply(@RequestHeader(value = PROOF_HEADER, required = false) String proofToken,
			@RequestBody SaveAdmittanceCarApplyReqDTO request) {
		visitorTruckProofService.apply(proofToken, request);
		// 匿名提交端点只确认受理结果，绝不返回申请实体中的手机号、审批和接待人资料。
		return success(Boolean.TRUE);
	}
}
