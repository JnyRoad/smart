package com.tce.smart.app.service.fore.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.service.fore.SocialSecurityService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.resp.SearchSocialSecurityRespDTO;
import com.tce.smart.platform.api.feign.RemoteSocialSecurityService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SocialSecurityServiceImpl implements  SocialSecurityService {

	@Autowired
	private RemoteSocialSecurityService  remoteSocialSecurityService;

	@Override
	public Result<List<SearchSocialSecurityRespDTO>> getSmtSocialSecurityList() {
		// TODO Auto-generated method stub
		return remoteSocialSecurityService.getSmtSocialSecurityList(SecurityConstants.FROM_IN);
	}

}
