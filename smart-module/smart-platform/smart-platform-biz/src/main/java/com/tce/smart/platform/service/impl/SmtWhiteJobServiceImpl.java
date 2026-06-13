package com.tce.smart.platform.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.dto.WhiteJobRemoveDTO;
import com.tce.smart.platform.core.entity.SmtWhiteJob;
import com.tce.smart.platform.core.vo.WhiteJobVO;
import com.tce.smart.platform.core.mapper.SmtWhiteJobMapper;
import com.tce.smart.platform.service.SmtWhiteJobService;
import com.tce.smart.tool.exception.TCEException;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;

@Service
public class SmtWhiteJobServiceImpl extends ServiceImpl<SmtWhiteJobMapper, SmtWhiteJob> implements SmtWhiteJobService {

	@Autowired
	private SmtWhiteJobMapper mapper;

	@Override
	public Result removeVisitorById(WhiteJobRemoveDTO whiteJobRemoveDTO) {
		// TODO Auto-generated method stub
		if(whiteJobRemoveDTO.getIds().size()>0)
		{
			for (Integer id : whiteJobRemoveDTO.getIds()) {
				this.baseMapper.deleteById(id);
			}
		}
		return new Result<>(true);
	}

	@Override
	public Result saveWhiteJob(SmtWhiteJob smtWhiteJob) {
		// TODO Auto-generated method stub
		SmtWhiteJob selectOne = this.baseMapper.selectOne(Wrappers.<SmtWhiteJob> query().lambda().eq(SmtWhiteJob::getJobId,smtWhiteJob.getJobId()));
		if(ObjectUtil.isNotNull(selectOne))
		{
			  throw new TCEException("此岗位已加入到列表");
		}

		smtWhiteJob.setCreateTime(DateUtil.date());
		return new Result<>(smtWhiteJob.insert());
	}

	@Override
	public IPage<WhiteJobVO> page(Page page, SmtWhiteJob smtWhiteJob) {
		// TODO Auto-generated method stub
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		IPage<WhiteJobVO>  pagR=mapper.page(page,smtWhiteJob,parkIdList);
		return pagR;
	}

}
