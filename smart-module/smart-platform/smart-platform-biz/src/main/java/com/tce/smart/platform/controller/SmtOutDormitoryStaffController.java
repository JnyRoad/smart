package com.tce.smart.platform.controller;


import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.resp.DormitorySituationRespDTO;
import com.tce.smart.platform.service.SmtDormitoryStaffService;
import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.data.api.dto.ehrview.OvwYsCallOwanceDetailsDTO;
import com.tce.smart.platform.api.dto.req.SearchOutDormitoryReqDTO;
import com.tce.smart.platform.api.dto.resp.SearchOutDormitoryRespDTO;
import com.tce.smart.platform.core.entity.SmtOutDormitoryStaff;
import com.tce.smart.platform.core.vo.OutDormitoryDetailVO;
import com.tce.smart.platform.core.vo.SearchOutDormitoryVO;
import com.tce.smart.platform.service.SmtOutDormitoryStaffService;

import java.util.List;


/**
 * 员工外宿信息表
 *
 * @author 齐佩
 * @date 2019-04-18 14:32:40
 */
@RestController
@AllArgsConstructor
@RequestMapping("/out/dormitory/staff")
public class SmtOutDormitoryStaffController extends BaseController {
	@Autowired
	private SmtOutDormitoryStaffService outDormitoryStaffService;

	private final SmtDormitoryStaffService smtDormitoryStaffService;

	@Inner
	@GetMapping("status")
	public Result status(@RequestParam("staffBadge") String staffBadge) {
		return success(outDormitoryStaffService.status(staffBadge));
	}

	/**
	 * 申请外宿
	 *
	 * @param apply
	 * @return
	 */
	/*@Inner*/
	@SysLog("app接口申请外宿")
	@PostMapping("addOutDormitory")
	public Result addOutDormitory(@RequestBody SmtOutDormitoryStaff apply) {
		return outDormitoryStaffService.addOutDormitory(apply);
	}
	/**
	 * 获取外宿类型和计算规则
	 * @return
	 */
	@SysLog("app接口获取补贴信息")
	@GetMapping("getAllowance")
	@Inner
	public Result getAllowance(@RequestParam("staffBadge") String staffBadge, @RequestParam("type") Integer type) {
		return outDormitoryStaffService.getAllowance(staffBadge, type);
	}

	@SysLog("app接口查询外宿信息")
	@GetMapping("getOutDormitoryInfo")
	@Inner
	public Result getOutDormitoryInfo( @RequestParam("staffBadge") String staffBadge, @RequestParam("type") Integer type) {
		return outDormitoryStaffService.getOutDormitoryInfo(staffBadge, type);
	}

	@SysLog("app接口查询外宿审批详细信息")
	@GetMapping("detail")
	@Inner
	public Result<OutDormitoryDetailVO> outRoomApplyDetail(@RequestParam("id") Integer id) {
		return outDormitoryStaffService.getOutDormitoryInfoDetail(id);
	}

	@SysLog("app接口查询外宿审批详细信息")
	@GetMapping("detail/byId")
	@Inner
	public Result<OutDormitoryDetailVO> outRoomApplyDetailById( @RequestParam("recordId") String recordId) {
		return outDormitoryStaffService.outRoomApplyDetailById(recordId);
	}

	@SysLog("清理外宿审批撤销的记录")
	@GetMapping("refresh")
	@Inner
	public void refreshOutDormitory() {
		 outDormitoryStaffService.refreshOutDormitory();
	}

	@SysLog("查询外宿补贴申请表")
	@GetMapping("/page/list")
	public Result<IPage<SearchOutDormitoryRespDTO>> getOutDormitoryPageList(Page page, SearchOutDormitoryReqDTO searchOutDormitoryReqDTO) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		searchOutDormitoryReqDTO.setParkIds(parkIds);
		IPage<SearchOutDormitoryVO> outDormitoryPageList = outDormitoryStaffService.getOutDormitoryPageList(page,searchOutDormitoryReqDTO);
		return  success(outDormitoryPageList,SearchOutDormitoryRespDTO.class);
	}

	@GetMapping("/detail/{id}")
	public Result getOutDormitoryDetailById(@PathVariable("id") Integer id) {
		return  new Result<>(outDormitoryStaffService.getOutDormitoryDetailById(id));
	}

	@SysLog("查询外宿补贴开始时间的配置时间")
	@GetMapping("/getDormitroySet")
	public Result<Integer> getDormitroySet() {
	return  new Result<>(outDormitoryStaffService.getDormitroySet());
	}

	@SysLog("查询外宿补贴信息")
	@GetMapping("/callOwanceDetails")
	public Result<DormitorySituationRespDTO> getOvwYsCallOwanceDetails(@RequestParam("staffBadge") String staffBadge,
																	   @RequestParam("parkId") Integer parkId, @RequestParam("nowDor") String nowDor) {
		return success(outDormitoryStaffService.getOvwYsCallOwanceDetails(staffBadge, 11, parkId, nowDor,smtDormitoryStaffService));
	}
}
