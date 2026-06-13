package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.RegexUtils;
import com.tce.smart.platform.core.entity.SmtStaffFamily;
import com.tce.smart.platform.core.mapper.SmtStaffFamilyMapper;
import com.tce.smart.platform.service.SmtStaffFamilyService;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

/**
 * 员工人际关系表
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:26
 */
@AllArgsConstructor
@Service
public class SmtStaffFamilyServiceImpl extends ServiceImpl<SmtStaffFamilyMapper, SmtStaffFamily> implements SmtStaffFamilyService {


	private final SmtStaffFamilyMapper mapper;

	@Override
	public Result addStaffFamily(SmtStaffFamily smtStaffFamily) {
		// TODO Auto-generated method stub
		if(!RegexUtils.matchName(smtStaffFamily.getRelation()))
		{
			return new Result<>(Boolean.FALSE, "亲属关系只允许汉字、字母与数字的组合，最长为30个字符");
		}
		if(!RegexUtils.matchName(smtStaffFamily.getName()))
		{
			return new Result<>(Boolean.FALSE, "亲属姓名只允许汉字、字母与数字的组合，最长为30个字符");
		}
		return new Result<>(mapper.insert(smtStaffFamily));
	}

	@Override
	public Result updateStaffFamily(SmtStaffFamily smtStaffFamily) {
		// TODO Auto-generated method stub
		if(!RegexUtils.matchName(smtStaffFamily.getRelation()))
		{
			return new Result<>(Boolean.FALSE, "亲属关系只允许汉字、字母与数字的组合，最长为30个字符");
		}
		if(!RegexUtils.matchName(smtStaffFamily.getName()))
		{
			return new Result<>(Boolean.FALSE, "亲属姓名只允许汉字、字母与数字的组合，最长为30个字符");
		}
		return new Result<>(mapper.updateById(smtStaffFamily));
	}

	@Override
	public Result getByStaffId(String staffId) {
		return Result.builder().data(mapper.selectByStaffId(staffId)).build();
	}

	@Override
	public Result removeFamilyByStaffId(Long staffId) {
		// TODO Auto-generated method stub
		int delete = mapper.delete(Wrappers.<SmtStaffFamily> query().lambda().eq(SmtStaffFamily::getStaffId, staffId));
		return new Result<>(delete);
	}
}
