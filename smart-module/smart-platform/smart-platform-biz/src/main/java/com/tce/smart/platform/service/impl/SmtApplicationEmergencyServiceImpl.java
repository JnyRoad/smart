package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.dto.ApplicationEmergencyDTO;
import com.tce.smart.platform.core.entity.SmtApplicationEmergency;
import com.tce.smart.platform.core.mapper.SmtApplicationEmergencyMapper;
import com.tce.smart.platform.core.mapper.SmtApplicationMapper;
import com.tce.smart.platform.service.SmtApplicationEmergencyService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.Validate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 应聘者紧急联系人
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:30
 */
@AllArgsConstructor
@Service
public class SmtApplicationEmergencyServiceImpl extends ServiceImpl<SmtApplicationEmergencyMapper, SmtApplicationEmergency> implements SmtApplicationEmergencyService {

	private final SmtApplicationEmergencyMapper mapper;
	private final SmtApplicationMapper applicationMapper;

	@Override
	public Boolean saveApplicationEmergency(SmtApplicationEmergency smtApplicationEmergency) {
		// TODO Auto-generated method stub
//		if(!RegexUtils.matchName(smtApplicationEmergency.getRelation()))
//		{
//			return new Result<>(Boolean.FALSE, "联系人关系只允许汉字、字母与数字的组合，最长为30个字符");
//		}
//		if(!RegexUtils.matchName(smtApplicationEmergency.getEmergencyName()))
//		{
//			return new Result<>(Boolean.FALSE, "紧急联系人姓名只允许汉字、字母与数字的组合，最长为30个字符");
//		}
		return smtApplicationEmergency.insert();
	}

	@Override
	public Integer updateByIdApplicationEmergency(ApplicationEmergencyDTO emergencyDTO) {
		// TODO Auto-generated method stub

		List<SmtApplicationEmergency> selectList = mapper.selectList(Wrappers.<SmtApplicationEmergency>query().lambda().eq(SmtApplicationEmergency::getApplicationId,Long.parseLong(emergencyDTO.getApplicationId() )));
		SmtApplicationEmergency emergency=new SmtApplicationEmergency();
		/*if(!RegexUtils.matchName(emergencyDTO.getRelationType()))
		{
			return new Result<>(Boolean.FALSE, "联系人关系只允许汉字、字母与数字的组合，最长为30个字符");
		}
		if(!RegexUtils.matchName(emergencyDTO.getEmergencyName()))
		{
			return new Result<>(Boolean.FALSE, "紧急联系人姓名只允许汉字、字母与数字的组合，最长为30个字符");
		}
		if(!RegexUtils.matchPhone(emergencyDTO.getEmergencyPhone()))
		{
			return new Result<>(Boolean.FALSE, "请输入正确的电话");
		}*/

		if(selectList.size()>0)
		{
			emergency=selectList.get(0);
			emergency.setEmergencyName(emergencyDTO.getEmergencyName());
			emergency.setRelation(emergencyDTO.getRelation());
			emergency.setTelephont(emergencyDTO.getPhone());
		}
		return mapper.updateById(emergency);
	}

	@Override
	public Result getByApplicationId(String applicationId) {
		Validate.isTrue(applicationId != null, "应聘者ID不能为空");
		SmtApplicationEmergency ApplicationEmergency = mapper.selectByApplicationId(applicationId);
		return Result.builder().data(ApplicationEmergency).build();
	}


}
