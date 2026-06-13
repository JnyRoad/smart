package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.RegexUtils;
import com.tce.smart.platform.core.entity.SmtApplicationWork;
import com.tce.smart.platform.core.mapper.SmtApplicationWorkMapper;
import com.tce.smart.platform.service.SmtApplicationWorkService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 应聘者工作经验
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:10
 */
@Service
@AllArgsConstructor
public class SmtApplicationWorkServiceImpl extends ServiceImpl<SmtApplicationWorkMapper, SmtApplicationWork> implements SmtApplicationWorkService {

	private final SmtApplicationWorkMapper mapper;


	@Override
	public Result addApplicationWork(SmtApplicationWork smtApplicationWork) {
		// TODO Auto-generated method stub

		if(!RegexUtils.matchName(smtApplicationWork.getJobName()))
		{
			return new Result<>(Boolean.FALSE, "职位只允许汉字、字母与数字的组合，最长为30个字符");
		}
		if(!RegexUtils.matchName(smtApplicationWork.getCompany()))
		{
			return new Result<>(Boolean.FALSE, "公司名称只允许汉字、字母与数字的组合，最长为30个字符");
		}

		return new Result<>(smtApplicationWork.insert());
	}

	@Override
	public Result updateApplicationWork(SmtApplicationWork smtApplicationWork) {
		// TODO Auto-generated method stub
		if(!RegexUtils.matchName(smtApplicationWork.getJobName()))
		{
			return new Result<>(Boolean.FALSE, "职位只允许汉字、字母与数字的组合，最长为30个字符");
		}
		if(!RegexUtils.matchName(smtApplicationWork.getCompany()))
		{
			return new Result<>(Boolean.FALSE, "公司名称只允许汉字、字母与数字的组合，最长为30个字符");
		}

		return new Result<>(smtApplicationWork.updateById());
	}

	@Override
	public List<SmtApplicationWork> getSmtApplicationWorkList(String applicationId) {
		// TODO Auto-generated method stub
		List<SmtApplicationWork> selectList = this.list(Wrappers.<SmtApplicationWork> query().lambda().eq(SmtApplicationWork::getApplicationId, applicationId));
		 return selectList;
	}

	@Override
	public Boolean deleteApplicationWorkList(String applicationId) {
		// TODO Auto-generated method stub

		return this.remove(Wrappers.<SmtApplicationWork> query().lambda().eq(SmtApplicationWork::getApplicationId, applicationId));
	}




}
