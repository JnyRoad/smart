package com.tce.smart.app.controller.fore;

import com.tce.smart.app.ao.fore.AllApplicationAo;
import com.tce.smart.app.ao.fore.VacateClassAo;
import com.tce.smart.app.service.fore.VacateService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.AddAskLeavelApplicationReqDTO;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 请假申请
 * @author 梁圆
 */
@Api(tags = "请假申请")
@RestController
@AllArgsConstructor
@RequestMapping("")
public class VacateController extends BaseController{

	private VacateService vacateService;
	/**
	 * 查看时长单位
	 * @return
	 */
	@GetMapping("/application/vacate/unit")
	public Result getUnitByVacateType(@RequestParam String vacateCode) {
		return new Result<>(vacateService.getUnitByVacateType(vacateCode));
	}
	/**
	 * 查看请假类型
	 * @return
	 */
	@GetMapping("/application/vacate/type")
	public Result getVacateType() {
		return new Result<>(vacateService.getVacateType());
	}

	/**
	 * 查询班次信息
	 * @return
	 */
	@PostMapping("/application/classes/query")
	public Result getVacateClasses(@RequestBody VacateClassAo vacateClassAo) {
		return new Result<>(vacateService.getVacateClasses(vacateClassAo));
	}

	/**
	 * 分页获取请假列表
	 *
	 * @param params     分页参数
	 * @return
	 */
	@GetMapping("/process/vacate/record/list")
	public Result getVacateList(@RequestParam Map<String, Object> params) {
		return new Result<>(vacateService.getVacateList(params));
	}

	/**
	 * 查看请假的详情
	 * @param vacateAoId
	 * @return
	 */
	@PostMapping("/process/vacate/record/detail")
	public Result getVacateDetail(@RequestBody AllApplicationAo vacateAoId) {
		return success(vacateService.getVacateDetail(vacateAoId));
	}
	/**
	 * 添加请假申请
	 * @return
	 */
	@PostMapping("/application/vacate")
	public Result addVacate(@RequestBody AddAskLeavelApplicationReqDTO addAskLeavelApplicationDTO) {
		vacateService.addVacate(addAskLeavelApplicationDTO);
		return success();
	}
}
