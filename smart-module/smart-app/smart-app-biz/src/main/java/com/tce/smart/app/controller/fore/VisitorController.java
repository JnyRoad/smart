package com.tce.smart.app.controller.fore;

import com.tce.smart.app.ao.fore.ApproveVisitorAo;
import com.tce.smart.app.ao.fore.VisitorAo;
import com.tce.smart.app.ao.fore.VisitorIdAo;
import com.tce.smart.app.ao.wechat.AddVisitMemberAo;
import com.tce.smart.app.ao.wechat.AddVisitorAo;
import com.tce.smart.app.service.fore.VisitorService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 访客信息Controller
 *
 * @author梁圆
 * @date 2019-05-13 14:46:11
 */
@RestController
@AllArgsConstructor
@RequestMapping("/visit")
public class VisitorController extends BaseController {

	private VisitorService visitorService;

	/**
	 * 分页获取来访预约列表
	 *
	 * @param params     分页参数
	 * @param visitorAo 查询条件
	 * @return
	 */
	@PostMapping("/list")
	public Result<?> getVisitorList(@RequestParam Map<String, Object> params, @RequestBody VisitorAo visitorAo) {
		return new Result<>(visitorService.getVisitorList(params, visitorAo));
	}

	/**
	 * 查看来访预约详情
	 * @param visitorId
	 * @return
	 */
	@PostMapping("/detail")
	public Result<?> getVisitorListDeatil(@RequestBody VisitorIdAo visitorId) {
		return success(visitorService.getVisitorListDeatil(visitorId));
	}

	/**
	 * 查看随行人员
	 * @param visitorId
	 * @return
	 */
	@PostMapping("/member/list")
	public Result<?> getMemberListDeatil(@RequestBody VisitorIdAo visitorId) {
		return success(visitorService.getMemberListDeatil(visitorId));
	}
	/**
	 * 添加访客预约
	 * @param addVisitorAo addVisitorAo
	 * @return
	 */
	@PostMapping("/add")
	public Result<?> addVisitor(@RequestBody AddVisitorAo addVisitorAo) {
		return new Result<>(visitorService.addVisitor(addVisitorAo));
	}
	/**
	 * 添加随行人员预约
	 * @param addVisitMemberAo addVisitMemberAo
	 * @return
	 */
	@PostMapping("/member/add")
	public Result<?> addFellowVisitor(@RequestBody AddVisitMemberAo addVisitMemberAo) {
		visitorService.addFellowVisitor(addVisitMemberAo);
		return success();
	}
	/**
	 * 获取来访事由
	 * @return
	 */
	@GetMapping("/reason/type")
	public Result<?> getVisitorType() {
		return success(visitorService.getVisitorType());
	}

	/**
	 * 获取来访事由
	 * @return
	 */
	@GetMapping("/refuse/type")
	public Result<?> getVisitorRefuseType() {
		return visitorService.getVisitorRefuseType();
	}

	/**
	 *获取该员工的未审批个数
	 * @return
	 */
	@GetMapping("/approve/getToApprovalCount")
	public Result<?> getToApprovalCount() {
		return success(visitorService.getToApprovalCount());
	}
	/**
	 * 访客审核
	 * @return
	 */
	@PostMapping("/approve")
	public Result<?> approveVisitorByVisitId(@RequestBody ApproveVisitorAo approveVisitorAo) {
		return new Result<>(visitorService.approveVisitorByVisitId(approveVisitorAo));
	}

	@GetMapping("/getParks")
	public Result<?> getParks(@RequestParam(value = "staffBadge", required = false) String staffBadge) {
		return visitorService.getParks(staffBadge);
	}

}
