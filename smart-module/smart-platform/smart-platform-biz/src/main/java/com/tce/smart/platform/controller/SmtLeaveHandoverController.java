package com.tce.smart.platform.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

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
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.dto.LeaveHandoverDTO;
import com.tce.smart.platform.core.entity.SmtLeaveApplication;
import com.tce.smart.platform.core.entity.SmtLeaveHandover;
import com.tce.smart.platform.core.model.LeaveHandoverDep;
import com.tce.smart.platform.core.model.LeaveHandoverDepJjr;
import com.tce.smart.platform.core.vo.LeaveHandoverApplicationVO;
import com.tce.smart.platform.service.SmtLeaveHandoverService;

import cn.hutool.core.collection.CollectionUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestHeader;


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

	private static final String APP_LEAVE_SELF_PURPOSE = "app-leave-self";

	@Autowired
	private OpenApiAuthenticationAdapter authenticationAdapter;

	@Value("${security.inner.leave-app.app-client-id:}")
	private String appServiceClientId;

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
	  @Inner
	  @OpenApi("server")
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
	  @Inner
	  @OpenApi("server")
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
	@Inner
	@OpenApi("server")
	@GetMapping("/get/item/{jjr}/{processId}")
	public Result getLeaveHandoverItemByJjr(@PathVariable("jjr") String jjr,@PathVariable("processId") String processId,
			@RequestHeader("X-Smart-Actor-Badge") String actorBadge,
			@RequestHeader(value = "X-Smart-Actor-Park-Ids", required = false) String actorParkIds,
			@RequestHeader(value = SecurityConstants.FROM, required = false) String from,
			@RequestHeader(value = "X-Smart-Internal-Purpose", required = false) String purpose){
		assertAppAssigneeCaller(jjr, actorBadge, from, purpose);
	  SmtLeaveApplication leaveApplication = smtLeaveApplicationService.getOne(new LambdaQueryWrapper<SmtLeaveApplication>().eq(SmtLeaveApplication::getProcessId,processId));
		if (leaveApplication == null || leaveApplication.getParkId() == null
				|| !parseActorParks(actorParkIds).contains(leaveApplication.getParkId())) {
			throw new AccessDeniedException("离职交接不存在或无权访问");
		}
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

	/** 旧交接项响应保持兼容，但不再允许任意 caller 指定 jjr 读取。 */
	private void assertAppAssigneeCaller(String jjr, String actorBadge, String from, String purpose) {
		Authentication authentication = SecurityUtils.getAuthentication();
		if (!jjr.equals(actorBadge) || !SecurityConstants.FROM_IN.equals(from)
				|| !APP_LEAVE_SELF_PURPOSE.equals(purpose) || appServiceClientId == null || appServiceClientId.trim().isEmpty()
				|| authentication == null || !authenticationAdapter.isClientOnly(authentication)
				|| !appServiceClientId.equals(authenticationAdapter.clientId(authentication))) {
			throw new AccessDeniedException("App 离职交接内部调用未获授权");
		}
	}

	/** 旧交接项兼容路由也必须使用 App 会话派生的园区范围。 */
	private Set<Integer> parseActorParks(String actorParkIds) {
		Set<Integer> parks = new HashSet<>();
		if (StrUtil.isBlank(actorParkIds)) {
			return parks;
		}
		for (String value : actorParkIds.split(",")) {
			try {
				parks.add(Integer.valueOf(value.trim()));
			} catch (RuntimeException ignored) {
				return new HashSet<>();
			}
		}
		return parks;
	}

	/**
	 * 确认工作交接
	 * @param leaveHandoverDTO 交接内容
	 * @return
	 */
	  @SysLog("确认工作交接")
	  @Inner
	  @OpenApi("server")
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
	  @Inner
	  @OpenApi("server")
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
	  @Inner
	  @OpenApi("server")
	  @GetMapping("/end/{processId}")
  public Result closeLeaveHandover(@PathVariable("processId") String processId) {
	return new Result<>(smtLeaveHandoverService.closeLeaveHandover(processId));

  }
}
