package com.tce.smart.platform.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.dto.VisitorPushEamilDTO;
import com.tce.smart.platform.core.entity.SmtVisitorPushEamil;
import com.tce.smart.platform.core.mapper.SmtVisitorPushEamilMapper;
import com.tce.smart.platform.service.SmtVisitorPushEamilService;
import com.tce.smart.tool.exception.TCEException;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 访客消息推送接收email
 * @author QIPEI
 *
 */

@Slf4j
@Service
public class SmtVisitorPushEamilServiceImpl extends ServiceImpl<SmtVisitorPushEamilMapper, SmtVisitorPushEamil> implements SmtVisitorPushEamilService {

	@Override
	public List<SmtVisitorPushEamil> searchAll(SmtVisitorPushEamil smtVisitorPushEamil) {
			// TODO Auto-generated method stub
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		if(parkIdList.size()>0)
		{
			List<SmtVisitorPushEamil> selectAll = this.baseMapper.selectList(Wrappers.<SmtVisitorPushEamil> query().lambda().eq(SmtVisitorPushEamil::getParkId, smtVisitorPushEamil.getParkId()).in(SmtVisitorPushEamil::getParkId,parkIdList));
			return selectAll;
		}
		else
		{
			return this.list();
		}
	}


	@Override
	public Boolean add(VisitorPushEamilDTO emails) {
		// TODO Auto-generated method stub

		if(StringUtils.isEmpty(emails)){
			throw new TCEException("记录推送人列表为空");
		}
		if(emails.getEmails().size()>0)
		{
			for (SmtVisitorPushEamil smtVisitorPushEamil : emails.getEmails()) {
				smtVisitorPushEamil.setType(emails.getType());
				smtVisitorPushEamil.setParkId(emails.getParkId());
				smtVisitorPushEamil.insert();
			}
		}
		return true;
	}


	@Override
	public Boolean update(VisitorPushEamilDTO emails) {
		// TODO Auto-generated method stub

		if(StringUtils.isEmpty(emails)){
			throw new TCEException("记录推送人列表为空");
		}
		//删除后重新添加
		this.baseMapper.delete(Wrappers.<SmtVisitorPushEamil> query().lambda().eq(SmtVisitorPushEamil::getParkId, emails.getParkId()));
		if(emails.getEmails().size()>0)
		{
			if(Objects.isNull(emails.getType())) {
				emails.setType(1);
			}
			for (SmtVisitorPushEamil smtVisitorPushEamil : emails.getEmails()) {
				smtVisitorPushEamil.setType(emails.getType());
				smtVisitorPushEamil.setParkId(emails.getParkId());
				smtVisitorPushEamil.insert();
			}
		}
		return true;
	}



}
