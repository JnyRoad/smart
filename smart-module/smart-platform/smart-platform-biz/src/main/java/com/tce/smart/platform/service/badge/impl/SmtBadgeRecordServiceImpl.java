package com.tce.smart.platform.service.badge.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.dto.InternalUserSummaryRespDTO;
import com.tce.smart.admin.api.feign.RemoteUserInternalService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.entity.badge.SmtBadgeRecord;
import com.tce.smart.platform.core.mapper.SmtBadgeRecordMapper;
import com.tce.smart.platform.service.badge.SmtBadgeRecordService;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.BadgeOperaStatusEnum;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 厂牌补领流程表
 *
 * @author fushiping
 * @date 2020-07-07 11:47:27
 */
@Service
public class SmtBadgeRecordServiceImpl extends ServiceImpl<SmtBadgeRecordMapper, SmtBadgeRecord> implements SmtBadgeRecordService {

	@Autowired
	private RemoteUserInternalService remoteUserInternalService;

	@Override
	public Boolean insertRecord(String badge, Long applyId, Integer status) {
		String username;
		if(status.equals(BadgeOperaStatusEnum.APPLY.getCode())) {
			username = badge;
		}else {
			//获取登录用户
			username = SecurityUtils.getUser().getUsername();
		}
		Result<InternalUserSummaryRespDTO> result = remoteUserInternalService.summary(username);
		InternalUserSummaryRespDTO user = result.getData();
		if(Objects.isNull(user)) {
			throw new SmartException("用户信息关联失败");
		}
		SmtBadgeRecord record = new SmtBadgeRecord();
		record.setCreaterId(user.getUserId());
		record.setCreaterName(username);
		//设置用户角色信息
		if(CollectionUtils.isNotEmpty(user.getRoleNames())) {
			List<String> roleIds = user.getRoleNames();
			String roleStr = StringUtils.join(roleIds, SymbolConstants.COMMA);
			record.setCreateRole(roleStr);
		}
		record.setCreateTime(LocalDateTime.now());
		record.setApplyId(applyId);
		record.setOperateType(status);
		return this.save(record);
	}
}
