package com.tce.smart.app.controller;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.data.api.feign.attendance.RemoteDepartmentService;
import com.tce.smart.data.api.feign.ehrview.*;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-14 17:53
 */
@RestController
@AllArgsConstructor
@RequestMapping("/appehr")
public class AppEhrController extends BaseController {
	private final RemoteEvwHortationsAllService remoteEvwHortationsAllService;
	private final RemoteEvwAcardlostAllService remoteEvwAcardlostAllService;
	private final RemoteEvwBizAregotRegisterService remoteEvwBizAregotRegisterService;
	private final RemoteEvwBizCallowanceFoodCancelService remoteEvwBizCallowanceFoodCancelService;
	private final RemoteEvwBizCallowanceFoodService remoteEvwBizCallowanceFoodService;
	private final RemoteEvwBizCallowanceService remoteEvwBizCallowanceService;
	private final RemoteEvwBizLcardlostService remoteEvwBizLcardlostService;
	private final RemoteEvwBizLdxregLeaveRegisterService remoteEvwBizLdxregLeaveRegisterService;
	private final RemoteEvwBizLregleaveRegisterService remoteEvwBizLregleaveRegisterService;
	private final RemoteEvwBizLregleaveService remoteEvwBizLregleaveService;
	private final RemoteEvwCallowanceAlltService remoteEvwCallowanceAlltService;
	private final RemoteEvwCallowanceCancelAlltService remoteEvwCallowanceCancelAlltService;
	private final RemoteEvwCotherAllowanceAllService remoteEvwCotherAllowanceAllService;
	private final RemoteEvwLdxRegLeaveAllService remoteEvwLdxRegLeaveAllService;
	private final RemoteEvwLergotAllService remoteEvwLergotAllService;
	private final RemoteDepartmentService remoteDepartmentService;

	/**
	 *  根据员工工号和月份查询奖惩记录
	 * @return
	 */
	@GetMapping("/hortationall/list")
	public Result hortationAllList(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		return success(remoteEvwHortationsAllService.list(badge, queryMonth, SecurityConstants.FROM_IN));
	}

	@GetMapping("/acardlostall/list")
	public Result acardlostAllList(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		return success(remoteEvwAcardlostAllService.list(badge, queryMonth, SecurityConstants.FROM_IN));
	}

	@GetMapping("/aregotregister/list")
	public Result aregotRegisterList(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		return success(remoteEvwBizAregotRegisterService.list(badge, queryMonth, SecurityConstants.FROM_IN));
	}

	@GetMapping("/callowancefoodcancel/list")
	public Result callowanceFoodCancelList(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		return success(remoteEvwBizCallowanceFoodCancelService.list(badge, queryMonth, SecurityConstants.FROM_IN));
	}

	@GetMapping("/callowancefood/list")
	public Result callowanceFoodList(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		return success(remoteEvwBizCallowanceFoodService.list(badge, queryMonth, SecurityConstants.FROM_IN));
	}

	@GetMapping("/callowance/list")
	public Result callowanceList(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		return success(remoteEvwBizCallowanceService.list(badge, queryMonth, SecurityConstants.FROM_IN));
	}

	@GetMapping("/lcardlost/list")
	public Result lcardlostList(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		return success(remoteEvwBizLcardlostService.list(badge, queryMonth, SecurityConstants.FROM_IN));
	}

	@GetMapping("/ldxregleaveregister/list")
	public Result ldxregLeaveRegisterList(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		return success(remoteEvwBizLdxregLeaveRegisterService.list(badge, queryMonth, SecurityConstants.FROM_IN));
	}

	@GetMapping("/lregleaveregister/list")
	public Result lregleaveRegisterList(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		return success(remoteEvwBizLregleaveRegisterService.list(badge, queryMonth, SecurityConstants.FROM_IN));
	}

	@GetMapping("/lregleave/list")
	public Result lregleaveList(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		return success(remoteEvwBizLregleaveService.list(badge, queryMonth, SecurityConstants.FROM_IN));
	}

	@GetMapping("/callowanceallt/list")
	public Result callowanceAlltList(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		return success(remoteEvwCallowanceAlltService.list(badge, queryMonth, SecurityConstants.FROM_IN));
	}

	@GetMapping("/callowancecancelallt/list")
	public Result callowanceCancelAlltList(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		return success(remoteEvwCallowanceCancelAlltService.list(badge, queryMonth, SecurityConstants.FROM_IN));
	}

	@GetMapping("/cotherallowanceall/list")
	public Result cotherAllowanceAllList(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		return success(remoteEvwCotherAllowanceAllService.list(badge, queryMonth, SecurityConstants.FROM_IN));
	}

	@GetMapping("/ldxregleaveall/list")
	public Result ldxregLeaveAllList(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		return success(remoteEvwLdxRegLeaveAllService.list(badge, queryMonth, SecurityConstants.FROM_IN));
	}

	@GetMapping("/lergotall/list")
	public Result lergotAllList(@RequestParam("badge") String badge, @RequestParam("queryMonth") String queryMonth){
		return success(remoteEvwLergotAllService.list(badge, queryMonth, SecurityConstants.FROM_IN));
	}

	@GetMapping("/acardlostall/detail")
	public Result acardlostAllDetail(@RequestParam("badge") String badge, @RequestParam("kqStartDate") String kqStartDate){
		return success(remoteEvwAcardlostAllService.getByBadge(badge, kqStartDate, SecurityConstants.FROM_IN));
	}

	@GetMapping("/aregotregister/detail")
	public Result aregotRegisterDetail(@RequestParam("badge") String badge, @RequestParam("otterm") String otterm){
		return success(remoteEvwBizAregotRegisterService.getByBadge(badge, otterm, SecurityConstants.FROM_IN));
	}

	@GetMapping("/callowancefoodcancel/detail")
	public Result callowanceFoodCancelDetail(@RequestParam("badge") String badge, @RequestParam("backDate") String backDate){
		return success(remoteEvwBizCallowanceFoodCancelService.getByBadge(badge, backDate, SecurityConstants.FROM_IN));
	}

	@GetMapping("/callowancefood/detail")
	public Result callowanceFoodDetail(@RequestParam("badge") String badge, @RequestParam("beginDate") String beginDate){
		return success(remoteEvwBizCallowanceFoodService.getByBadge(badge, beginDate, SecurityConstants.FROM_IN));
	}

	@GetMapping("/callowance/detail")
	public Result callowanceDetail(@RequestParam("badge") String badge, @RequestParam("beginDate") String beginDate){
		return success(remoteEvwBizCallowanceService.getByBadge(badge, beginDate, SecurityConstants.FROM_IN));
	}

	@GetMapping("/lcardlost/detail")
	public Result lcardlostDetail(@RequestParam("badge") String badge, @RequestParam("kqStartDate") String kqStartDate){
		return success(remoteEvwBizLcardlostService.getByBadge(badge, kqStartDate, SecurityConstants.FROM_IN));
	}

	@GetMapping("/ldxregleaveregister/detail")
	public Result ldxregLeaveRegisterDetail(@RequestParam("badge") String badge, @RequestParam("beginTime") String beginDate){
		return success(remoteEvwBizLdxregLeaveRegisterService.getByBadge(badge, beginDate, SecurityConstants.FROM_IN));
	}

	@GetMapping("/lregleaveregister/detail")
	public Result lregleaveRegisterDetail(@RequestParam("badge") String badge, @RequestParam("beginTime") String beginTime){
		return success(remoteEvwBizLregleaveRegisterService.getByBadge(badge, beginTime, SecurityConstants.FROM_IN));
	}

	@GetMapping("/lregleave/detail")
	public Result lregleaveDetail(@RequestParam("badge") String badge, @RequestParam("beginTime") String beginTime){
		return success(remoteEvwBizLregleaveService.getByBadge(badge, beginTime, SecurityConstants.FROM_IN));
	}

	@GetMapping("/callowanceallt/detail")
	public Result callowanceAlltDetail(@RequestParam("badge") String badge, @RequestParam("beginDate") String beginDate){
		return success(remoteEvwCallowanceAlltService.getByBadge(badge, beginDate, SecurityConstants.FROM_IN));
	}

	@GetMapping("/callowancecancelallt/detail")
	public Result callowanceCancelAlltDetail(@RequestParam("badge") String badge, @RequestParam("backDate") String backDate){
		return success(remoteEvwCallowanceCancelAlltService.getByBadge(badge, backDate, SecurityConstants.FROM_IN));
	}

	@GetMapping("/cotherallowanceall/detail")
	public Result cotherAllowanceAllDetail(@RequestParam("badge") String badge, @RequestParam("beginDate") String regDate){
		return success(remoteEvwCotherAllowanceAllService.getByBadge(badge, regDate, SecurityConstants.FROM_IN));
	}

	@GetMapping("/ldxregleaveall/detail")
	public Result ldxregLeaveAllDetail(@RequestParam("badge") String badge, @RequestParam("beginTime") String beginTime){
		return success(remoteEvwLdxRegLeaveAllService.getByBadge(badge, beginTime, SecurityConstants.FROM_IN));
	}

	@GetMapping("/lergotall/detail")
	public Result lergotAllDetail(@RequestParam("badge") String badge, @RequestParam("otteam") String otteam){
		return success(remoteEvwLergotAllService.getByBadge(badge, otteam, SecurityConstants.FROM_IN));
	}

	@GetMapping("/attendance/department/info")
	public Result info(@RequestParam("badge") String badge, @RequestParam("queryDate") String queryDate){
		return success(remoteDepartmentService.info(badge, queryDate, SecurityConstants.FROM_IN));
	}

	@GetMapping("/attendance/department/list")
	public Result list(@RequestParam("badge") String badge, @RequestParam("queryDate") String queryDate, @RequestParam("type") Integer type){
		return success(remoteDepartmentService.list(badge, queryDate, type, SecurityConstants.FROM_IN));
	}
}
