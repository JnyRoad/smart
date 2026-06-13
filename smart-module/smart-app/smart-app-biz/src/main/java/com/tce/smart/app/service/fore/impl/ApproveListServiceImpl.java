package com.tce.smart.app.service.fore.impl;

import org.springframework.stereotype.Service;

import com.tce.smart.app.service.fore.ApproveListService;
import com.tce.smart.app.vo.fore.ApproveListRequestVO;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.feign.RemoteApproveListService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 离职申请
 *
 * @author 王艳勇
 * @date 2019-05-13 16:17:32
 */
@Service
@AllArgsConstructor
@Slf4j
public class ApproveListServiceImpl implements ApproveListService {

	private RemoteApproveListService remoteApproveListService;

	@Override
	public Result getProcessRecord(ApproveListRequestVO approveListRequestVO) {
		String badge = SecurityUtils.getUser().getUsername();
		return remoteApproveListService.getProcessRecord(approveListRequestVO.getCurrent(), approveListRequestVO.getSize(), badge, approveListRequestVO.getRecordType(), approveListRequestVO.getRecordState(), SecurityConstants.FROM_IN);
	}

}
