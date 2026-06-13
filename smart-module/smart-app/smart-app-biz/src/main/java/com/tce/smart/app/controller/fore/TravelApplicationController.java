package com.tce.smart.app.controller.fore;

import com.tce.smart.app.ao.fore.AllApplicationAo;
import com.tce.smart.app.service.fore.TravelService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 出差申请
 * @author 梁圆
 */

@RestController
@AllArgsConstructor
@RequestMapping("travel")
public class TravelApplicationController extends BaseController{

	private TravelService travelService;


	/**
	 * 分页获取出差列表
	 *
	 * @param params  分页参数
	 * @return
	 */
	@GetMapping("/process/record/list")
	public Result getTravelList(@RequestParam Map<String, Object> params) {
		return travelService.getTravelList(params);
	}

	/**
	 * 查看出差的详情
	 * @param allApplicationAoId
	 * @return
	 */
	@PostMapping("/process/record/detail")
	public Result getTravelDetail(@RequestBody AllApplicationAo allApplicationAoId) {
		return travelService.getTravelDetail(allApplicationAoId);
	}
	/**
	 * 查看出差的日程
	 * @param allApplicationAoId
	 * @return
	 */
	@PostMapping("/process/record/infoDay")
	public Result getTravelInfoDay(@RequestBody AllApplicationAo allApplicationAoId) {
		return travelService.getTravelInfoDay(allApplicationAoId);
	}

	/**
	 * 查看出差的报告
	 * @param allApplicationAoId
	 * @return
	 */
	@PostMapping("/process/record/infoReport")
	public Result getTravelInfoReport(@RequestBody AllApplicationAo allApplicationAoId) {
		return travelService.getTravelInfoReport(allApplicationAoId);
	}
	/**
	 * 查看出差的流程
	 * @param allApplicationAoId
	 * @return
	 */
	@PostMapping("/process/record/infoFlow")
	public Result getTravelInfoFlow(@RequestBody AllApplicationAo allApplicationAoId) {
		return travelService.getTravelInfoFlow(allApplicationAoId);
	}
}
