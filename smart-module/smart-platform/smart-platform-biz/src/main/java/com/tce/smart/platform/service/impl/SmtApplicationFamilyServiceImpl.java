package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.RegexUtils;
import com.tce.smart.platform.core.entity.SmtApplicationFamily;
import com.tce.smart.platform.core.mapper.SmtApplicationFamilyMapper;
import com.tce.smart.platform.core.mapper.SmtApplicationMapper;
import com.tce.smart.platform.service.SmtApplicationFamilyService;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 应聘者人际关系表
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:26
 */
@AllArgsConstructor
@Service
public class SmtApplicationFamilyServiceImpl extends ServiceImpl<SmtApplicationFamilyMapper, SmtApplicationFamily> implements SmtApplicationFamilyService {


	private final SmtApplicationFamilyMapper mapper;

	private final SmtApplicationMapper appMapper;

	@Override
	public Result addApplicationFamily(SmtApplicationFamily smtApplicationFamily) {
		// TODO Auto-generated method stub
		if(!RegexUtils.matchName(smtApplicationFamily.getRelation()))
		{
			return new Result<>(Boolean.FALSE, "亲属关系只允许汉字、字母与数字的组合，最长为30个字符");
		}
		if(!RegexUtils.matchName(smtApplicationFamily.getName()))
		{
			return new Result<>(Boolean.FALSE, "亲属姓名只允许汉字、字母与数字的组合，最长为30个字符");
		}
		return new Result<>(mapper.insert(smtApplicationFamily));
	}

	@Override
	public Result updateApplicationFamily(SmtApplicationFamily smtApplicationFamily) {
		// TODO Auto-generated method stub
		if(!RegexUtils.matchName(smtApplicationFamily.getRelation()))
		{
			return new Result<>(Boolean.FALSE, "亲属关系只允许汉字、字母与数字的组合，最长为30个字符");
		}
		if(!RegexUtils.matchName(smtApplicationFamily.getName()))
		{
			return new Result<>(Boolean.FALSE, "亲属姓名只允许汉字、字母与数字的组合，最长为30个字符");
		}
		return new Result<>(mapper.updateById(smtApplicationFamily));
	}

	@Override
	public List<SmtApplicationFamily> getByApplicationId(String applicationId) {
		return mapper.selectByApplicationId(applicationId);
	}

	@Override
	public Result removeFamilyByApplicationId(Long applicationId) {
		// TODO Auto-generated method stub
		int delete = mapper.delete(Wrappers.<SmtApplicationFamily> query().lambda().eq(SmtApplicationFamily::getApplicationId, applicationId));
		 return new Result<>(delete);
	}


}
