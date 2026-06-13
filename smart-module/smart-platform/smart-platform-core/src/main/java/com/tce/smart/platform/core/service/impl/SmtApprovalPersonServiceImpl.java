package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.SmtApprovalPerson;
import com.tce.smart.platform.core.mapper.SmtApprovalPersonMapper;
import com.tce.smart.platform.core.service.SmtApprovalPersonService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-04-08 16:25:00
 */
@Service
public class SmtApprovalPersonServiceImpl extends ServiceImpl<SmtApprovalPersonMapper, SmtApprovalPerson> implements SmtApprovalPersonService {

	@Override
	public List<SmtApprovalPerson> getList(Integer nodeId) {
		return  this.list(Wrappers.<SmtApprovalPerson>query().lambda().eq(SmtApprovalPerson::getNodeId, nodeId));
	}
}
