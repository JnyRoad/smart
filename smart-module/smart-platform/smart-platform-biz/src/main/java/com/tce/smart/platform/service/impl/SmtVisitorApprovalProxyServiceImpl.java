package com.tce.smart.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.visitormanage.VisitorProxyQueryReqDTO;
import com.tce.smart.platform.api.dto.req.visitormanage.VisitorProxyReqDTO;
import com.tce.smart.platform.api.dto.req.visitormanage.VisitorWhiteQueryReqDTO;
import com.tce.smart.platform.api.dto.req.visitormanage.VisitorWhiteReqDTO;
import com.tce.smart.platform.api.dto.resp.visitormanage.VisitorProxyQueryRespDTO;
import com.tce.smart.platform.api.dto.resp.visitormanage.VisitorWhiteQueryRespDTO;
import com.tce.smart.platform.core.dto.VisitorProxyDTO;
import com.tce.smart.platform.core.dto.VisitorWhiteDTO;
import com.tce.smart.platform.core.entity.SmtVisitorApprovalProxy;
import com.tce.smart.platform.core.entity.SmtVisitorApprovalWhite;
import com.tce.smart.platform.core.mapper.SmtVisitorApprovalProxyMapper;
import com.tce.smart.platform.core.mapper.SmtVisitorApprovalWhiteMapper;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.SmtVisitorApprovalProxyService;
import com.tce.smart.platform.service.SmtVisitorApprovalWhiteService;
import com.tce.smart.tool.exception.TCEException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @description: SmtVisitorApprovalProxyServiceImpl
 * @date: 2020/12/29 15:38
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@Service
public class SmtVisitorApprovalProxyServiceImpl extends ServiceImpl<SmtVisitorApprovalProxyMapper, SmtVisitorApprovalProxy> implements SmtVisitorApprovalProxyService {

	@Resource
	private SmtStaffService smtStaffService;

	@Override
	public IPage<VisitorProxyQueryRespDTO> pageQuery(VisitorProxyQueryReqDTO visitorProxyQueryReqDTO) {
		VisitorProxyDTO proxyDTO = new VisitorProxyDTO();
		BeanUtil.copyProperties(visitorProxyQueryReqDTO,proxyDTO);
		proxyDTO.setParkIds(SecurityUtils.getUser().getParkIdList());
		Page searchPage = new Page(visitorProxyQueryReqDTO.getCurrent(), visitorProxyQueryReqDTO.getSize());
		IPage<VisitorProxyDTO> visitorProxyDTOIPage = this.baseMapper.pageQuery(searchPage, proxyDTO);
		IPage<VisitorProxyQueryRespDTO> queryRespDTOIPage = new Page<>(searchPage.getCurrent(),searchPage.getSize(),searchPage.getTotal());
		List<VisitorProxyQueryRespDTO> queryRespDTOList = new ArrayList<>();
		for(VisitorProxyDTO detail : visitorProxyDTOIPage.getRecords()){
			VisitorProxyQueryRespDTO queryRespDTO = new VisitorProxyQueryRespDTO();
			BeanUtil.copyProperties(detail,queryRespDTO);
			queryRespDTOList.add(queryRespDTO);
		}
		queryRespDTOIPage.setRecords(queryRespDTOList);
		return queryRespDTOIPage;
	}

	@Transactional
	@Override
	public Boolean saveProxy(VisitorProxyReqDTO visitorProxyReqDTO) {
		//验证被访人员工信息
		smtStaffService.getStffNoQuitByBadge(visitorProxyReqDTO.getInterVieweeBadge());
		//验证代理人员工信息
		smtStaffService.getStffNoQuitByBadge(visitorProxyReqDTO.getProxyBadge());
		//查询是否已添加
		SmtVisitorApprovalProxy approvalProxy = this.getOne(new LambdaQueryWrapper<SmtVisitorApprovalProxy>()
				.eq(SmtVisitorApprovalProxy::getIntervieweeBadge, visitorProxyReqDTO.getInterVieweeBadge())
				.eq(SmtVisitorApprovalProxy::getProxyBadge, visitorProxyReqDTO.getProxyBadge())
		);
		if(null != approvalProxy){
			throw new TCEException("记录已存在");
		}
		//保存审批代理
		SmtVisitorApprovalProxy proxy = new SmtVisitorApprovalProxy();
		proxy.setIntervieweeBadge(visitorProxyReqDTO.getInterVieweeBadge());
		proxy.setProxyBadge(visitorProxyReqDTO.getProxyBadge());
		proxy.setParkId(visitorProxyReqDTO.getParkId());
		proxy.setCreateTime(new Date());
		return this.save(proxy);
	}

	@Override
	public Boolean batchDel(List<Long> ids) {
		return this.removeByIds(ids);
	}
}
