package com.tce.smart.app.service.fore.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.service.fore.LeaveApplicationService;
import com.tce.smart.app.vo.fore.*;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.api.dto.*;
import com.tce.smart.platform.api.dto.req.LeaveApplicationReqDTO;
import com.tce.smart.platform.api.feign.RemoteLeaveApplicationService;
import com.tce.smart.platform.api.feign.RemoteLeaveHandoverService;
import com.tce.smart.tool.enums.LeaveApplicationEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 离职申请
 *
 * @author 王艳勇
 * @date 2019-05-13 16:17:32
 */
@Service
@AllArgsConstructor
@Slf4j
public class LeaveApplicationServiceImpl implements LeaveApplicationService {
	private static final String APP_LEAVE_SELF_PURPOSE = "app-leave-self";

	private RemoteLeaveApplicationService remoteLeaveApplicationService;
	private RemoteLeaveHandoverService remoteLeaveHandoverService;

    @Override
    public Result<?> save(LeaveApplicationVO leaveApplicationVO) {
		LeaveApplicationReqDTO leaveApplicationDTO = new LeaveApplicationReqDTO();

		String badge = currentActorBadge();

		leaveApplicationDTO.setApplyBadge(badge);
        leaveApplicationDTO.setBadge(badge);
        leaveApplicationDTO.setLeaveReason(leaveApplicationVO.getDimissionReason());
        leaveApplicationDTO.setLeaveType(leaveApplicationVO.getDimissionType());
        leaveApplicationDTO.setYearHoliday(leaveApplicationVO.getYearHoliday());
        leaveApplicationDTO.setLeaveTime(DateUtil.parse(leaveApplicationVO.getDimissionDate(), "yyyy-MM-dd"));
        leaveApplicationDTO.setLeaveStatus(leaveApplicationVO.getDimissionApplyType());
		Result<?> result = remoteLeaveApplicationService.saveForActor(leaveApplicationDTO, badge,
				currentActorParkIds(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED,
				APP_LEAVE_SELF_PURPOSE);
        return result;
    }

    @Override
    public Result<?> getLeaveType() {
        Result<List<LeaveTypeDTO>> result = remoteLeaveApplicationService.getLeaveType(SecurityConstants.FROM_IN);
        List<LeaveTypeDTO> list = result.getData();
        LeaveTypeDataVO<LeaveTypeDTO> leaveDicData = new LeaveTypeDataVO<>();
        leaveDicData.setRecords(list);
        leaveDicData.setTotal(list.size());
        return new Result<>(leaveDicData);
    }

    @Override
    public Result<?> getLeaveReason() {
        Result<List<LeaveReasonDTO>> result = remoteLeaveApplicationService.getLeaveReason(SecurityConstants.FROM_IN);
        List<LeaveReasonDTO> list = result.getData();
        LeaveTypeDataVO<LeaveReasonDTO> leaveDicData = new LeaveTypeDataVO<>();
        leaveDicData.setRecords(list);
        leaveDicData.setTotal(list.size());
        return new Result<>(leaveDicData);
    }

    @Override
    public Result<?> getYearHoliday() {
//        String badge = "1";// 获取员工号
        String badge=SecurityUtils.getUser().getUsername();
        return remoteLeaveApplicationService.getYearHoliday(badge,SecurityConstants.FROM_IN);
    }

    @Override
    public Result<?> getLeaveApplication(String processId) {
		Result<Map<String, Object>> result = remoteLeaveApplicationService.getForActor(processId, currentActorBadge(),
				currentActorParkIds(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED,
				APP_LEAVE_SELF_PURPOSE);
        Map<String,Object> leaveApplicationVO = result.getData();
        LeaveApplicationDetailVO leaveApplicationDetailVO = new LeaveApplicationDetailVO();
        leaveApplicationDetailVO.setEmployee(leaveApplicationVO);
        return new Result<>(leaveApplicationDetailVO);
    }

    @Override
    public Result<?> getProcessRecord(Page page,Integer dimissionApplyType) {
        String badge=SecurityUtils.getUser().getUsername();
        return remoteLeaveApplicationService.getProcessRecord(page.getCurrent(),page.getSize(), badge,dimissionApplyType,SecurityConstants.FROM_IN);
    }

    @Override
    public Result<?> getLeaveApplicationRecord(String recordId) {
		Result<Map<String,Object>> result = remoteLeaveApplicationService.getForActor(recordId, currentActorBadge(),
				currentActorParkIds(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED,
				APP_LEAVE_SELF_PURPOSE);
        Map<String,Object> leaveHandoverApplicationVO = result.getData();
		Result<List<ProcessRecordFlowDTO>> record = remoteLeaveApplicationService.getRecordForActor(recordId, currentActorBadge(),
				currentActorParkIds(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED,
				APP_LEAVE_SELF_PURPOSE);
        List<ProcessRecordFlowDTO> flow = record.getData();
        ProcessRecordVO processRecordVO = new ProcessRecordVO();
        processRecordVO.setEmployee(leaveHandoverApplicationVO);
        processRecordVO.setFlow(flow);
        return new Result<>(processRecordVO);
    }

	@Override
	public Result getLeaveHandover(String processId) {
		Result<List<LeaveHandoverDepJjrDTO>> result = remoteLeaveApplicationService.getHandoverForActor(processId, currentActorBadge(),
				currentActorParkIds(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED,
				APP_LEAVE_SELF_PURPOSE);
		Result<Map<String, Object>> resultLeaveApplication = remoteLeaveApplicationService.getForActor(processId, currentActorBadge(),
				currentActorParkIds(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED,
				APP_LEAVE_SELF_PURPOSE);
        Map<String,Object> leaveApplicationVO = resultLeaveApplication.getData();
        List<LeaveHandoverDepJjrDTO> list = result.getData();
        LeaveItemDataVO  LeaveItemDataVO = new LeaveItemDataVO();
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int workDays = day;
		if(day < 6){
			//当月到上月1号的天数
			calendar.set(Calendar.MONTH,-1);
			workDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) + day;
	}

		LeaveItemDataVO.setWorkDays(workDays);
        LeaveItemDataVO.setHandover(list);
        LeaveItemDataVO.setApproveStatus(leaveApplicationVO.get("approveState") != null ?Integer.parseInt(leaveApplicationVO.get("approveState").toString()) : null);
        return new Result<>(LeaveItemDataVO);
    }

	/** 离职流程中的 actor 只能来自 App 已认证会话，不能使用前端 employeeId。 */
	private String currentActorBadge() {
		String actorBadge = SecurityUtils.getUser().getUsername();
		if (actorBadge == null || actorBadge.trim().isEmpty()) {
			throw new IllegalStateException("当前登录员工不存在");
		}
		return actorBadge;
	}

	/** 内部调用只转发当前登录员工已有的园区范围，空范围直接失败。 */
	private String currentActorParkIds() {
		SmartUser user = SecurityUtils.getUser();
		if (user == null || user.getParkIdList() == null || user.getParkIdList().isEmpty()) {
			throw new IllegalStateException("当前登录员工未绑定园区");
		}
		List<String> parks = new ArrayList<>();
		for (Integer parkId : user.getParkIdList()) {
			if (parkId != null) {
				parks.add(String.valueOf(parkId));
			}
		}
		if (parks.isEmpty()) {
			throw new IllegalStateException("当前登录员工未绑定园区");
		}
		return String.join(",", parks);
	}

}
