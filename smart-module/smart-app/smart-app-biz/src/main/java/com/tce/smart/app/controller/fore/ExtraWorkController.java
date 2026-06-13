package com.tce.smart.app.controller.fore;

import com.tce.smart.app.ao.fore.AllApplicationAo;
import com.tce.smart.app.service.fore.ExtraWorkService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.AddOverTimeApplicationReqDTO;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 加班申请
 * @author 梁圆
 */

@RestController
@AllArgsConstructor
@RequestMapping("")
public class ExtraWorkController extends BaseController{

	private ExtraWorkService extraWorkService;

	/**
	 * 获取加班的类型
	 * @return
	 */
	@GetMapping("application/extrawork/type")
	public Result getExtraWorkType() {
		return new Result<>(extraWorkService.getExtraWorkType());
	}
	/**
	 * 获取班别的类型
	 * @return
	 */
	@GetMapping("/application/extrawork/class/type")
	public Result getExtraClassType() {
		return new Result<>(extraWorkService.getExtraClassType());
	}

	/**
	 * 分页获取加班列表
	 *
	 * @param params     分页参数
	 * @return
	 */
	@GetMapping("/process/extrawork/record/list")
	public Result getExtraWorkList(@RequestParam Map<String, Object> params) {
		return new Result<>(extraWorkService.getExtraWorkList(params));
	}

	/**
	 * 查看加班的详情
	 * @param allApplicationAoId
	 * @return
	 */
	@PostMapping("/process/extrawork/record/detail")
	public Result getExtraWorkDetail(@RequestBody AllApplicationAo allApplicationAoId) {
		return success(extraWorkService.getExtraWorkDetail(allApplicationAoId));
	}

	/**
	 * 添加加班申请
	 * @return
	 */
	@PostMapping("/application/extrawork")
	public Result<?> addExtraWork(@RequestBody AddOverTimeApplicationReqDTO addOverTimeApplicationDTO) {
		extraWorkService.addExtraWork(addOverTimeApplicationDTO);
		return success();
	}
}
