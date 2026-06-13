package com.tce.smart.app.service.fore.impl;

import com.tce.smart.app.service.fore.LeaveHandoverService;
import com.tce.smart.app.vo.fore.LeaveHandoverDetailVO;
import com.tce.smart.app.vo.fore.LeaveHandoverVO;
import com.tce.smart.app.vo.fore.LeaveItemVO;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
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

	private RemoteLeaveHandoverService remoteLeaveHandoverService;

    @Override
    public Result<?> getLeaveHandoverByJjr(String processId) {
        String badge=SecurityUtils.getUser().getUsername();
        Result<Map<String,Object>> result = remoteLeaveHandoverService.getLeaveHandoverByProcessId(processId,SecurityConstants.FROM_IN);
        Map<String,Object> leaveHandoverApplicationVO = result.getData();
        Result<Map<String,Object>> item = remoteLeaveHandoverService.getLeaveHandoverItemByJjr(badge, processId,SecurityConstants.FROM_IN);
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
        return remoteLeaveHandoverService.endLeaveHandover(leaveHandoverDTO,SecurityConstants.FROM_IN);
    }

	@Override
	public Result<?> startLeaveHandover(String processId) {
		return remoteLeaveHandoverService.startLeaveHandover(processId,SecurityConstants.FROM_IN);
	}

	@Override
	public Result<?> commitLeaveHandover(String processId) {
		return remoteLeaveHandoverService.closeLeaveHandover(processId,SecurityConstants.FROM_IN);
	}
}
