package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.dto.StaffEmergencyDTO;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtStaffEmergency;
import com.tce.smart.platform.core.mapper.SmtStaffEmergencyMapper;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.service.SmtStaffEmergencyService;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.Validate;
import org.springframework.stereotype.Service;

/**
 * 员工紧急联系人
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:30
 */
@AllArgsConstructor
@Service
public class SmtStaffEmergencyServiceImpl extends ServiceImpl<SmtStaffEmergencyMapper, SmtStaffEmergency> implements SmtStaffEmergencyService {

	private final SmtStaffEmergencyMapper mapper;
	private final SmtStaffMapper staffMapper;

	@Override
	public Result saveStaffEmergency(SmtStaffEmergency smtStaffEmergency) {
		// TODO Auto-generated method stub
//		if(!RegexUtils.matchName(smtStaffEmergency.getRelation()))
//		{
//			return new Result<>(Boolean.FALSE, "联系人关系只允许汉字、字母与数字的组合，最长为30个字符");
//		}
//		if(!RegexUtils.matchName(smtStaffEmergency.getEmergencyName()))
//		{
//			return new Result<>(Boolean.FALSE, "紧急联系人姓名只允许汉字、字母与数字的组合，最长为30个字符");
//		}
		return new Result<>(smtStaffEmergency.insert());
	}

	@Override
	public Result updateByIdStaffEmergency(StaffEmergencyDTO emergencyDTO) {
		// TODO Auto-generated method stub

		SmtStaff selectOne = staffMapper.selectOne(Wrappers.<SmtStaff> query().lambda().eq(SmtStaff::getBadge, emergencyDTO.getBadge()));
		if (Objects.isNull(selectOne)) {
			throw new TCEException("未找到员工信息");
		}

		List<SmtStaffEmergency> selectList = mapper.selectList(Wrappers.<SmtStaffEmergency>query().lambda().eq(SmtStaffEmergency::getStaffId, selectOne.getId()));
		SmtStaffEmergency emergency=new SmtStaffEmergency();
//		if(!RegexUtils.matchName(emergencyDTO.getRelation()))
//		{
//			return new Result<>(Boolean.FALSE, "联系人关系只允许汉字、字母与数字的组合，最长为30个字符");
//		}
//		if(!RegexUtils.matchName(emergencyDTO.getEmergencyName()))
//		{
//			return new Result<>(Boolean.FALSE, "紧急联系人姓名只允许汉字、字母与数字的组合，最长为30个字符");
//		}
//		if(!RegexUtils.matchPhone(emergencyDTO.getEmergencyPhone()))
//		{
//			return new Result<>(Boolean.FALSE, "请输入正确的电话");
//		}

		if(selectList.size()>0)
		{
			emergency=selectList.get(0);
			emergency.setEmergencyName(emergencyDTO.getEmergencyName());
			emergency.setRelation(emergencyDTO.getRelation());
			emergency.setTelephont(emergencyDTO.getEmergencyPhone());
			return new Result<>(emergency.updateById());
		}
		else {
			emergency.setStaffId(selectOne.getId());
			emergency.setEmergencyName(emergencyDTO.getEmergencyName());
			emergency.setRelation(emergencyDTO.getRelation());
			emergency.setTelephont(emergencyDTO.getEmergencyPhone());
			return new Result<>(emergency.insert());
		}
	}

	@Override
	public Result getByStaffId(String staffId) {
		Validate.isTrue(staffId != null, "员工ID不能为空");
		SmtStaffEmergency staffEmergency = mapper.selectByStaffId(staffId);
		return Result.builder().data(staffEmergency).build();
	}
}
