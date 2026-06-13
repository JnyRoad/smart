package com.tce.smart.platform.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.AddFeedBackReqDTO;
import com.tce.smart.platform.core.dto.FeedBackQueryDTO;
import com.tce.smart.platform.core.entity.SmtFeedBack;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.mapper.SmtFeedBackMapper;
import com.tce.smart.platform.core.vo.FeedBackQueryVO;
import com.tce.smart.platform.service.SmtFeedBackService;
import com.tce.smart.platform.service.SmtStaffService;

import cn.hutool.core.bean.BeanUtil;
@Service
public class SmtFeedBackServiceImpl  extends ServiceImpl<SmtFeedBackMapper, SmtFeedBack> implements SmtFeedBackService{

	@Autowired
	private SmtStaffService smtStaffService;

	@Override
	public IPage<SmtFeedBack> page(Page page, FeedBackQueryDTO feedBackQueryDTO) {
		// TODO Auto-generated method stub
		return this.baseMapper.selectPage(page, feedBackQueryDTO);
	}

	@Override
	public Boolean updateSmtFeedBack(SmtFeedBack smtFeedBack) {
		// TODO Auto-generated method stub

		String username = SecurityUtils.getUser().getUsername();
		SmtFeedBack selectById = this.baseMapper.selectById(smtFeedBack.getId());
		selectById.setOperator(username);
		selectById.setStatus(1);
		selectById.setReply(smtFeedBack.getReply());
		selectById.setOperateTime(LocalDateTime.now());
		return selectById.updateById();
	}

	@Override
	public Boolean addSmtFeedBack(AddFeedBackReqDTO feedBack) {
		// TODO Auto-generated method stub
		SmtFeedBack  smtFeedBack=new SmtFeedBack();
		SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda()
				.eq(SmtStaff::getBadge, feedBack.getStaffBadge()));
		smtFeedBack.setStaffBadge(feedBack.getStaffBadge());
		smtFeedBack.setStaffName(selectOne.getName());
		smtFeedBack.setStaffPhone(selectOne.getPhone());
		smtFeedBack.setCreateTime(LocalDateTime.now());
		smtFeedBack.setQuestion(feedBack.getQuestion());
		smtFeedBack.setStatus(0);
		return smtFeedBack.insert();
	}

	@Override
	public FeedBackQueryVO getDetailById(Integer id) {
		// TODO Auto-generated method stub

		SmtFeedBack selectById = this.baseMapper.selectById(id);
		FeedBackQueryVO vo=new FeedBackQueryVO();
		BeanUtil.copyProperties(selectById, vo);

		SmtStaff selectOne = smtStaffService.getOne(Wrappers.<SmtStaff> query().lambda()
				.eq(SmtStaff::getBadge, selectById.getStaffBadge()));
		vo.setDepName(selectOne.getDepName());
		vo.setCompName(selectOne.getCompName());
		return vo;
	}



}
