package com.tce.smart.platform.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.platform.core.entity.SmtLbejConfig;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.model.LeaveHandoverItemJjrVO;
import com.tce.smart.platform.core.service.SmtLeaveApplicationService;
import com.tce.smart.platform.service.ILeaveApplicationService;
import com.tce.smart.platform.service.SmtLbejConfigService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.tool.constant.LeaveHandoverConstants;
import com.tce.smart.tool.util.ToolUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.dto.LeaveHandoverDTO;
import com.tce.smart.platform.core.entity.SmtLeaveApplication;
import com.tce.smart.platform.core.entity.SmtLeaveHandover;
import com.tce.smart.platform.core.model.LeaveHandoverDep;
import com.tce.smart.platform.core.model.LeaveHandoverDepJjr;
import com.tce.smart.platform.core.vo.LeaveHandoverApplicationVO;
import com.tce.smart.platform.service.SmtLeaveHandoverService;

import cn.hutool.core.collection.CollectionUtil;
import lombok.AllArgsConstructor;


/**
 * 工作交接表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:51
 */
@RestController
@AllArgsConstructor
@RequestMapping("/leave/handover")
public class SmtLeaveHandoverController extends BaseController{

  private final  SmtLeaveHandoverService smtLeaveHandoverService;

  private final SmtLbejConfigService smtLbejConfigService;

  private final SmtStaffService smtStaffService;

  private final SmtLeaveApplicationService smtLeaveApplicationService;

  private final ILeaveApplicationService leaveApplicationService;

  /**
   * 开始交接（审批完成触发）
   * @param processId 流程Id
   * @return
   */
  @SysLog("开始交接")
  @GetMapping("/start/{processId}")
  public Result startLeaveHandover(@PathVariable("processId") String processId) {
	return new Result<>(smtLeaveHandoverService.startLeaveHandover(processId));

  }

  /**
   * 获取交接内容
   * @param processId 流程号
   * @return
   */
  @SysLog("获取交接信息")
  @GetMapping("/get/{processId}")
  public Result getLeaveHandoverByProcessId(@PathVariable("processId") String processId) {
    SmtLeaveApplication leaveApplication = smtLeaveHandoverService.getLeaveHandoverByProcessId(processId);
	return success(leaveApplication, LeaveHandoverApplicationVO.class);
  }

  /**
   * 获取交接項目
   * @param jjr 交接人工号
   * @param processId 流程编号
   * @return
   */
  @SysLog("获取交接項目")
  @GetMapping("/get/item/{jjr}/{processId}")
  public Result getLeaveHandoverItemByJjr(@PathVariable("jjr") String jjr,@PathVariable("processId") String processId){
	  SmtLeaveApplication leaveApplication = smtLeaveApplicationService.getOne(new LambdaQueryWrapper<SmtLeaveApplication>().eq(SmtLeaveApplication::getProcessId,processId));
      List<SmtLeaveHandover> list = smtLeaveHandoverService.list(new LambdaQueryWrapper<SmtLeaveHandover>()
			  .eq(SmtLeaveHandover::getApplicationId,leaveApplication.getId())
			  .eq(SmtLeaveHandover::getJjr,jjr)
	  );
      if(CollectionUtil.isNotEmpty(list)) {

		  SmtStaff staff = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda()
				  .eq(SmtStaff::getBadge, leaveApplication.getBadge()));

		  LeaveHandoverDep leaveHandoverDep = new LeaveHandoverDep();
		  leaveHandoverDep.setDeptName(list.get(0).getZrdepName());
		  List<LeaveHandoverItemJjrVO> handItem = new ArrayList<>();
		  LeaveHandoverItemJjrVO leaveHandoverDepItemVO = null;
		  for (SmtLeaveHandover smtLeaveHandover : list) {
			  leaveHandoverDepItemVO = new LeaveHandoverItemJjrVO();

			  SmtLbejConfig lbejConfig = smtLbejConfigService.getOne(new LambdaQueryWrapper<SmtLbejConfig>()
					  .eq(SmtLbejConfig::getItemId, smtLeaveHandover.getJjItemId())
					  .eq(SmtLbejConfig::getDepId, staff.getDepId())
			  );
			  if(null != lbejConfig){
				  //该交接项在伙食费配置列表中 计算伙食费
				  Date currDate = new Date();
				  int today = DateUtil.dayOfMonth(currDate);
				  //本月一号
				  Date startDate = ToolUtils.setCalDate(currDate,Calendar.DAY_OF_MONTH,1);
				  if(today < 6){
					  //当前日期小于6 开始日期为上月1日
					  startDate = ToolUtils.setCalDate(ToolUtils.getCalDate(startDate, Calendar.MONTH,-1),Calendar.DAY_OF_MONTH,1);
				  }
				  BigDecimal calMealFee = leaveApplicationService.calMealFee(staff, startDate, new Date());
				  leaveHandoverDepItemVO.setItemAmt(calMealFee.setScale(2, RoundingMode.HALF_UP).doubleValue());
			  }

			  leaveHandoverDepItemVO.setItemDesc(StrUtil.isBlank(smtLeaveHandover.getJjRemark())?"":smtLeaveHandover.getJjRemark());
			  leaveHandoverDepItemVO.setItemId(smtLeaveHandover.getId());
			  leaveHandoverDepItemVO.setItemName(smtLeaveHandover.getJjItem());
			  leaveHandoverDepItemVO.setItemState(smtLeaveHandover.getJjClosed());
			  handItem.add(leaveHandoverDepItemVO);
		  }
		  leaveHandoverDep.setHandItem(handItem);

	  return success(leaveHandoverDep);
      }
      return success(null);
  }

	/**
	 * 确认工作交接
	 * @param leaveHandoverDTO 交接内容
	 * @return
	 */
  @SysLog("确认工作交接")
  @PostMapping("/commit")
  public Result endLeaveHandover(@RequestBody LeaveHandoverDTO leaveHandoverDTO) {
	return new Result<>(smtLeaveHandoverService.endLeaveHandover(leaveHandoverDTO));
  }

  /**
   * 查看工作交接
   * @param leaveApplication
   * @return
   */
  @SysLog("查看工作交接")
  @PostMapping("/detail")
  public Result getLeaveHandover(@RequestBody SmtLeaveApplication leaveApplication){
      List<SmtLeaveHandover> list = smtLeaveHandoverService.getLeaveHandover(leaveApplication.getProcessId());
      return success(list,LeaveHandoverDepJjr.class);
  }

  /**
   * 提交工作交接（交接完成触发）
   * @param processId 流程Id
   * @return
   */
  @SysLog("开始交接")
  @GetMapping("/end/{processId}")
  public Result closeLeaveHandover(@PathVariable("processId") String processId) {
	return new Result<>(smtLeaveHandoverService.closeLeaveHandover(processId));

  }
}
