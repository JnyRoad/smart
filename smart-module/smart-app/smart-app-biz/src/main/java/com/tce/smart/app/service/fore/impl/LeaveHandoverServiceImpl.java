package com.tce.smart.app.service.fore.impl;

import com.tce.smart.app.service.fore.LeaveHandoverService;
import com.tce.smart.app.vo.fore.LeaveHandoverDetailVO;
import com.tce.smart.app.vo.fore.LeaveHandoverVO;
import com.tce.smart.app.vo.fore.LeaveItemVO;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.api.dto.req.LeaveHandoverItemReqDTO;
import com.tce.smart.platform.api.dto.req.LeaveHandoverReqDTO;
import com.tce.smart.platform.api.feign.RemoteLeaveHandoverService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
public class LeaveHandoverServiceImpl implements LeaveHandoverService {
	private static final String APP_LEAVE_SELF_PURPOSE = "app-leave-self";

	private RemoteLeaveHandoverService remoteLeaveHandoverService;

    @Override
    public Result<?> getLeaveHandoverByJjr(String processId) {
        String badge=SecurityUtils.getUser().getUsername();
		Result<Map<String,Object>> result = remoteLeaveHandoverService.getHandoverForAssignee(processId, badge,
				currentActorParkIds(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED,
				APP_LEAVE_SELF_PURPOSE);
        Map<String,Object> leaveHandoverApplicationVO = result.getData();
		Result<Map<String,Object>> item = remoteLeaveHandoverService.getLeaveHandoverItemByJjr(badge, processId, badge,
				currentActorParkIds(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED,
				APP_LEAVE_SELF_PURPOSE);
        Map<String,Object> LeaveHandoverDep = item.getData();
        LeaveHandoverDetailVO leaveHandoverDetailVO = new LeaveHandoverDetailVO();
        leaveHandoverDetailVO.setEmployee(leaveHandoverApplicationVO);
        leaveHandoverDetailVO.setHandover(LeaveHandoverDep);
        return new Result<>(leaveHandoverDetailVO);
    }

    @Override
    public Result<?> endLeaveHandover(LeaveHandoverVO leaveHandoverVO) {
        String badge=SecurityUtils.getUser().getUsername();
		LeaveHandoverReqDTO leaveHandoverDTO = new LeaveHandoverReqDTO();
        leaveHandoverDTO.setProcessId(leaveHandoverVO.getProcessId());
        List<LeaveItemVO> leaveItemList = leaveHandoverVO.getHandItem();
        List<LeaveHandoverItemReqDTO> itemList = new ArrayList<>();
		LeaveHandoverItemReqDTO leaveHandoverItemDTO = null;
        for (LeaveItemVO leaveItemVO : leaveItemList) {
            leaveHandoverItemDTO = new LeaveHandoverItemReqDTO();
            leaveHandoverItemDTO.setJjItemId(leaveItemVO.getItemId());
            leaveHandoverItemDTO.setJjRemark(leaveItemVO.getItemDesc());
            leaveHandoverItemDTO.setJe(leaveItemVO.getItemAmt());
            itemList.add(leaveHandoverItemDTO);
        }
		leaveHandoverDTO.setJjr(badge);
		leaveHandoverDTO.setItemList(itemList);
		return remoteLeaveHandoverService.endHandoverForActor(leaveHandoverDTO, badge,
				currentActorParkIds(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED,
				APP_LEAVE_SELF_PURPOSE);
    }

	@Override
	public Result<?> startLeaveHandover(String processId) {
		return remoteLeaveHandoverService.startHandoverForActor(processId, SecurityUtils.getUser().getUsername(),
				currentActorParkIds(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED,
				APP_LEAVE_SELF_PURPOSE);
	}

	@Override
	public Result<?> commitLeaveHandover(String processId) {
		return remoteLeaveHandoverService.closeHandoverForActor(processId, SecurityUtils.getUser().getUsername(),
				currentActorParkIds(), SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED,
				APP_LEAVE_SELF_PURPOSE);
	}

	/** 交接流程同样必须由登录会话提供园区范围，不能接收前端传参。 */
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
