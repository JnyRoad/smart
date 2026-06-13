package com.tce.smart.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.visitormanage.VisitorWhiteQueryReqDTO;
import com.tce.smart.platform.api.dto.req.visitormanage.VisitorWhiteReqDTO;
import com.tce.smart.platform.api.dto.resp.visitormanage.VisitorWhiteQueryRespDTO;
import com.tce.smart.platform.core.dto.VisitorWhiteDTO;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtVisitorApprovalWhite;
import com.tce.smart.platform.core.mapper.SmtVisitorApprovalWhiteMapper;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.SmtVisitorApprovalWhiteService;
import com.tce.smart.tool.enums.StaffStatusEnum;
import com.tce.smart.tool.exception.TCEException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @description: SmtDormitoryApplyServiceImpl
 * @date: 2020/12/29 15:38
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@Service
public class SmtVisitorApprovalWhiteServiceImpl extends ServiceImpl<SmtVisitorApprovalWhiteMapper, SmtVisitorApprovalWhite> implements SmtVisitorApprovalWhiteService {

	@Resource
	private SmtStaffService smtStaffService;

	@Override
	public IPage<VisitorWhiteQueryRespDTO> pageQuery(VisitorWhiteQueryReqDTO visitorWhiteReqDTO) {
		VisitorWhiteDTO whiteDTO = new VisitorWhiteDTO();
		BeanUtil.copyProperties(visitorWhiteReqDTO,whiteDTO);
		whiteDTO.setParkIds(SecurityUtils.getUser().getParkIdList());
		Page searchPage = new Page(visitorWhiteReqDTO.getCurrent(), visitorWhiteReqDTO.getSize());
		IPage<VisitorWhiteDTO> visitorWhiteDTOIPage = this.baseMapper.pageQuery(searchPage, whiteDTO);
		IPage<VisitorWhiteQueryRespDTO> queryRespDTOIPage = new Page<>(searchPage.getCurrent(),searchPage.getSize(),searchPage.getTotal());
		List<VisitorWhiteQueryRespDTO> queryRespDTOList = new ArrayList<>();
		for(VisitorWhiteDTO detail : visitorWhiteDTOIPage.getRecords()){
			VisitorWhiteQueryRespDTO queryRespDTO = new VisitorWhiteQueryRespDTO();
			BeanUtil.copyProperties(detail,queryRespDTO);
			queryRespDTOList.add(queryRespDTO);
		}
		queryRespDTOIPage.setRecords(queryRespDTOList);
		return queryRespDTOIPage;
	}

	@Transactional
	@Override
	public Boolean saveItem(VisitorWhiteReqDTO visitorWhiteReqDTO) {
		//验证员工信息
		smtStaffService.getStffNoQuitByBadge(visitorWhiteReqDTO.getStaffBadge());
		//查询是否已添加
		SmtVisitorApprovalWhite approvalWhite = this.getOne(new LambdaQueryWrapper<SmtVisitorApprovalWhite>()
				.eq(SmtVisitorApprovalWhite::getStaffBadge, visitorWhiteReqDTO.getStaffBadge())
				.eq(SmtVisitorApprovalWhite::getParkId, visitorWhiteReqDTO.getParkId())
		);
		if(null != approvalWhite){
			throw new TCEException("该员工已添加");
		}
		//保存白名单
		SmtVisitorApprovalWhite item = new SmtVisitorApprovalWhite();
		item.setStaffBadge(visitorWhiteReqDTO.getStaffBadge());
		item.setParkId(visitorWhiteReqDTO.getParkId());
		item.setCreateTime(new Date());
		return this.save(item);
	}

	@Override
	public Boolean batchDel(List<Long> ids) {
		return this.removeByIds(ids);
	}
}
