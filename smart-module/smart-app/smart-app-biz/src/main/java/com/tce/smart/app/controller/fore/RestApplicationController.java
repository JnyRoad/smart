package com.tce.smart.app.controller.fore;

import com.tce.smart.app.ao.fore.AllApplicationAo;
import com.tce.smart.app.service.fore.RestApplicationService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.AddBreakOffApplicationReqDTO;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 调休申请
 * @author 梁圆
 */

@RestController
@AllArgsConstructor
@RequestMapping("")
public class RestApplicationController extends BaseController{

	private RestApplicationService restService;

	/**
	 * 获取调休剩余天数
	 * @return
	 */
	@GetMapping("/rest/balance/adjust/get")
	public Result getAdjust( @RequestParam(value = "staffBadge", required = false) String staffBadge) {
		return new Result<>(restService.getAdjust(staffBadge));
	}
	/**
	 * 获取调休类型
	 * @return
	 */
	@GetMapping("application/rest/type")
	public Result getRestType() {
		return new Result<>(restService.getRestType());
	}

     /**
	 * 分页获取调休列表
	 *
	 * @param params     分页参数
	 * @return
	 */
	@GetMapping("process/rest/record/list")
	public Result getRestList(@RequestParam Map<String, Object> params,@RequestParam(value = "staffBadge", required = false) String staffBadge) {
		return new Result<>(restService.getRestList(params,staffBadge));
	}


	/**
	 * 查看调休的详情
	 * @param allApplicationAoId
	 * @return
	 */
	@PostMapping("/process/rest/record/detail")
	public Result getRestDetail(@RequestBody AllApplicationAo allApplicationAoId) {
		return success(restService.getRestDetail(allApplicationAoId));
	}

	/**
	 * 添加调休申请
	 * @return
	 */
	@PostMapping("/application/rest")
	public Result<?> addRest(@RequestBody AddBreakOffApplicationReqDTO addBreakOffApplicationDTO) {
		restService.addRest(addBreakOffApplicationDTO);
		return success();
	}
}
