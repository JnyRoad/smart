package com.tce.smart.platform.core.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtApprovalNode;
import com.tce.smart.platform.core.mapper.SmtApprovalNodeMapper;
import com.tce.smart.platform.core.service.SmtApprovalNodeService;
import com.tce.smart.tool.enums.ApprovalPersonRuleEnum;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 *
 *
 * @author fushiping
 * @date 2021-04-08 16:25:18
 */
@Service
public class SmtApprovalNodeServiceImpl extends ServiceImpl<SmtApprovalNodeMapper, SmtApprovalNode> implements SmtApprovalNodeService {

	@Override
	public List<SmtApprovalNode> getList(Integer approvalId) {
		return this.list(Wrappers.<SmtApprovalNode>query().lambda().eq(SmtApprovalNode::getApprovalId, approvalId).orderByAsc(SmtApprovalNode::getSort));
	}

}
