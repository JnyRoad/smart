package com.tce.smart.schedule.service.platform.impl;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.platform.api.feign.RemoteLeaveApplicationService;
import com.tce.smart.schedule.service.platform.ILeaveApplicationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 离职申请表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:51
 */
@Service
@AllArgsConstructor
@Slf4j
public class LeaveApplicationServiceImpl implements ILeaveApplicationService {

	private final RemoteLeaveApplicationService remoteLeaveApplicationService;

	@Override
	public void syncProcessRecord() {
		remoteLeaveApplicationService.sysnProcessRecord(SecurityConstants.FROM_IN);
	}
}
