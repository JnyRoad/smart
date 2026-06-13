package com.tce.smart.platform.service;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.AddSocialSecurityReqDTO;
import com.tce.smart.platform.api.dto.resp.SearchSocialSecurityRespDTO;
import com.tce.smart.platform.core.entity.SmtSocialSecurity;

public interface SmtSocialSecurityService extends IService<SmtSocialSecurity> {


	IPage<SmtSocialSecurity> getSmtSocialSecurityPage(Page page, SmtSocialSecurity smtSocialSecurity);

	Boolean save(AddSocialSecurityReqDTO addSocialSecurityReqDTO);

	Boolean update(AddSocialSecurityReqDTO addSocialSecurityReqDTO);

	SearchSocialSecurityRespDTO detailById(String id);

	List<SearchSocialSecurityRespDTO> getSmtSocialSecurityList();

}
