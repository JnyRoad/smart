package com.tce.smart.platform.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.platform.api.dto.req.AddSocialSecurityReqDTO;
import com.tce.smart.platform.api.dto.resp.SearchSocialSecurityRespDTO;
import com.tce.smart.platform.core.entity.SmtSocialSecurity;
import com.tce.smart.platform.core.mapper.SmtSocialSecurityMapper;
import com.tce.smart.platform.service.SmtSocialSecurityService;

import cn.hutool.core.bean.BeanUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@AllArgsConstructor
public class SmtSocialSecurityServiceImpl  extends ServiceImpl<SmtSocialSecurityMapper, SmtSocialSecurity> implements SmtSocialSecurityService{

	@Override
	public IPage<SmtSocialSecurity> getSmtSocialSecurityPage(Page page, SmtSocialSecurity smtSocialSecurity) {
		// TODO Auto-generated method stub
		return this.baseMapper.selectPage(page, Wrappers.<SmtSocialSecurity> query().lambda().like(StringUtils.isNotBlank(smtSocialSecurity.getTitle()), SmtSocialSecurity::getTitle,smtSocialSecurity.getTitle()));
	}


	@Override
	public Boolean save(AddSocialSecurityReqDTO addSocialSecurityReqDTO) {
		// TODO Auto-generated method stub
		SmtSocialSecurity  smtSocialSecurity=new SmtSocialSecurity();
		smtSocialSecurity.setTitle(addSocialSecurityReqDTO.getTitle());
		smtSocialSecurity.setUrl(addSocialSecurityReqDTO.getUrl());
		smtSocialSecurity.setCreateTime(LocalDateTime.now());
		byte[] decodeFromString = Base64Utils.decodeFromString(addSocialSecurityReqDTO.getImage().replace("data:image/jpeg;base64,",""));
		smtSocialSecurity.setImage(decodeFromString);
		return smtSocialSecurity.insert();
	}


	@Override
	public Boolean update(AddSocialSecurityReqDTO addSocialSecurityReqDTO) {
		// TODO Auto-generated method stub
		SmtSocialSecurity  smtSocialSecurity=new SmtSocialSecurity();
		smtSocialSecurity.setId(addSocialSecurityReqDTO.getId());
		smtSocialSecurity.setTitle(addSocialSecurityReqDTO.getTitle());
		smtSocialSecurity.setUrl(addSocialSecurityReqDTO.getUrl());
		smtSocialSecurity.setCreateTime(LocalDateTime.now());
		byte[] decodeFromString = Base64Utils.decodeFromString(addSocialSecurityReqDTO.getImage().replace("data:image/jpeg;base64,",""));
		smtSocialSecurity.setImage(decodeFromString);
		return smtSocialSecurity.updateById();
	}


	@Override
	public SearchSocialSecurityRespDTO detailById(String id) {
		// TODO Auto-generated method stub
		SmtSocialSecurity selectById = this.baseMapper.selectById(id);
		SearchSocialSecurityRespDTO dto=new SearchSocialSecurityRespDTO();
		BeanUtil.copyProperties(selectById, dto);
		dto.setImage(Base64Utils.encodeToString(selectById.getImage()));
		return dto;
	}


	@Override
	public List<SearchSocialSecurityRespDTO> getSmtSocialSecurityList() {
		// TODO Auto-generated method stub
		SmtSocialSecurity so=new SmtSocialSecurity();
		List<SmtSocialSecurity> records = so.selectAll();
		List<SearchSocialSecurityRespDTO> list=new ArrayList<SearchSocialSecurityRespDTO>();
		for (SmtSocialSecurity smtSocialSecurity : records) {
			SearchSocialSecurityRespDTO dto=new SearchSocialSecurityRespDTO();
			BeanUtil.copyProperties(smtSocialSecurity, dto);
			dto.setImage(Base64Utils.encodeToString(smtSocialSecurity.getImage()));
			list.add(dto);
		}

		return list;
	}

}
