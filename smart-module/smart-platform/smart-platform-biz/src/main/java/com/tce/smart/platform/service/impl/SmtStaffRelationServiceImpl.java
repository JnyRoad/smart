package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.RegexUtils;
import com.tce.smart.platform.core.entity.SmtStaffRelation;
import com.tce.smart.platform.core.mapper.SmtStaffRelationMapper;
import com.tce.smart.platform.service.SmtStaffRelationService;

import lombok.AllArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 员工人际关系表
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:26
 */
@AllArgsConstructor
@Service
@Slf4j
public class SmtStaffRelationServiceImpl extends ServiceImpl<SmtStaffRelationMapper, SmtStaffRelation> implements SmtStaffRelationService {


	private final SmtStaffRelationMapper mapper;
	@Override
	public Result addStaffRelation(SmtStaffRelation smtStaffRelation) {
		// TODO Auto-generated method stub
		if(!RegexUtils.matchName(smtStaffRelation.getRelation()))
		{
			return new Result<>(Boolean.FALSE, "人事关系只允许汉字、字母与数字的组合，最长为30个字符");
		}
		if(!RegexUtils.matchName(smtStaffRelation.getName()))
		{
			return new Result<>(Boolean.FALSE, "联系人姓名只允许汉字、字母与数字的组合，最长为30个字符");
		}
		return new Result<>(mapper.insert(smtStaffRelation));
	}

	@Override
	public Result updateStaffRelation(SmtStaffRelation smtStaffRelation) {
		// TODO Auto-generated method stub
		if(!RegexUtils.matchName(smtStaffRelation.getRelation()))
		{
			return new Result<>(Boolean.FALSE, "人事关系只允许汉字、字母与数字的组合，最长为30个字符");
		}
		if(!RegexUtils.matchName(smtStaffRelation.getName()))
		{
			return new Result<>(Boolean.FALSE, "联系人姓名只允许汉字、字母与数字的组合，最长为30个字符");
		}
		return new Result<>(mapper.updateById(smtStaffRelation));
	}

	@Override
	public Result getByStaffId(String staffId) {
		log.info("获取员工关系staffId:{}",staffId);
		return Result.builder().data(mapper.selectByStaffId(staffId)).build();
	}

	@Override
	public Result removeRelationByStaffId(Integer staffId) {
		// TODO Auto-generated method stub
		int delete = mapper.delete(Wrappers.<SmtStaffRelation> query().lambda().eq(SmtStaffRelation::getStaffId, staffId));
		 return new Result<>(delete);
	}
}
