package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtApplicationEmail;
import com.tce.smart.platform.core.mapper.SmtApplicationEmailMapper;
import com.tce.smart.platform.service.SmtApplicationEmailService;
import com.tce.smart.tool.util.RegexUtils;

import cn.hutool.core.util.ObjectUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

/**
 * 应聘者邮箱
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:10
 */
@Service
@AllArgsConstructor
@Slf4j
public class SmtApplicationEmailServiceImpl extends ServiceImpl<SmtApplicationEmailMapper, SmtApplicationEmail> implements SmtApplicationEmailService {

	private final SmtApplicationEmailMapper mapper;

	@Override
	public Result<SmtApplicationEmail> getSmtApplicationEmailList(String applicationId) {
		// TODO Auto-generated method stub
		if(ObjectUtil.isNull(applicationId))
		{
			log.info("applicationId 不能为空");
			return new Result<SmtApplicationEmail>();
		}
		log.info("获取应聘者关系ApplicationId:{}",applicationId);
		SmtApplicationEmail selectById = mapper.selectOne(Wrappers.<SmtApplicationEmail> query().lambda().eq(SmtApplicationEmail::getApplicationId, applicationId));
		return new Result<SmtApplicationEmail>(selectById);

	}

	@Override
	public Result deleteApplicationEmailList(String applicationId) {
		// TODO Auto-generated method stub
		if(ObjectUtil.isNull(applicationId))
		{
			return new Result<>(Boolean.FALSE, "应聘者唯一标识为空");
		}

		int delete = mapper.delete(Wrappers.<SmtApplicationEmail> query().lambda().eq(SmtApplicationEmail::getApplicationId, applicationId));
		return new Result<>(delete);
	}

	@Override
	public Result addApplicationEmailList(SmtApplicationEmail email) {
		// TODO Auto-generated method stub
		if(ObjectUtil.isNull(email))
		{
			return new Result<>(Boolean.FALSE, "邮箱信息不能为空");
		}
		if(ObjectUtil.isNull(email.getApplicationId()))
		{
			return new Result<>(Boolean.FALSE, "应聘者唯一标识为空");
		}

		if(!RegexUtils.matchEmail(email.getEmail()))
		{
			return new Result<>(Boolean.FALSE, "邮箱格式不正确");
		}

		SmtApplicationEmail selectById = mapper.selectOne(Wrappers.<SmtApplicationEmail> query().lambda().eq(SmtApplicationEmail::getApplicationId, email.getApplicationId()));
		if(selectById!=null)
		{
			selectById.setEmail(email.getEmail());
			return new Result<>(mapper.updateById(selectById));
		}

		return new Result<>(mapper.insert(email));
	}

	@Override
	public Result updateApplicationEmailList(SmtApplicationEmail email) {
		// TODO Auto-generated method stub
		if(ObjectUtil.isNull(email))
		{
			return new Result<>(Boolean.FALSE, "邮箱信息不能为空");
		}
		if(ObjectUtil.isNull(email.getApplicationId()))
		{
			return new Result<>(Boolean.FALSE, "应聘者唯一标识为空");
		}

		if(!RegexUtils.matchEmail(email.getEmail()))
		{
			return new Result<>(Boolean.FALSE, "邮箱格式不正确");
		}

		SmtApplicationEmail selectById = mapper.selectOne(Wrappers.<SmtApplicationEmail> query().lambda().eq(SmtApplicationEmail::getApplicationId, email.getApplicationId()));
		selectById.setEmail(email.getEmail());
		return new Result<>(mapper.updateById(selectById));
	}







}
