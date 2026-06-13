package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtApplicationEducation;
import com.tce.smart.platform.core.mapper.SmtApplicationEducationMapper;
import com.tce.smart.platform.core.mapper.SmtApplicationMapper;
import com.tce.smart.platform.core.vo.EducationVO;
import com.tce.smart.platform.service.SmtApplicationEducationService;
import com.tce.smart.tool.constant.DictConstants;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 应聘者教育经验
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:38
 */
@Service
@AllArgsConstructor
public class SmtApplicationEducationServiceImpl extends ServiceImpl<SmtApplicationEducationMapper, SmtApplicationEducation> implements SmtApplicationEducationService {

	private final SmtApplicationEducationMapper mapper;

	private final SmtApplicationMapper appMapper;

	@Autowired
	private RemoteDictService remoteDictService;

	@Override
	public Result saveEducation(SmtApplicationEducation smtApplicationEducation) {
		// TODO Auto-generated method stub

//		if(!RegexUtils.matchName(smtApplicationEducation.getSchoolName()))
//		{
//			return new Result<>(Boolean.FALSE, "学校名称只允许汉字、字母与数字的组合，最长为30个字符");
//		}
//		if(!RegexUtils.matchName(smtApplicationEducation.getMajor()))
//		{
//			return new Result<>(Boolean.FALSE, "专业只允许汉字、字母与数字的组合，最长为30个字符");
//		}
		smtApplicationEducation.setGradType(1);
		return new Result<>(smtApplicationEducation.insert());
	}

	@Override
	public Result updateApplicationeEducation(SmtApplicationEducation smtApplicationEducation) {
		// TODO Auto-generated method stub
//		if(!RegexUtils.matchName(smtApplicationEducation.getSchoolName()))
//		{
//			return new Result<>(Boolean.FALSE, "学校名称只允许汉字、字母与数字的组合，最长为30个字符");
//		}
//		if(!RegexUtils.matchName(smtApplicationEducation.getMajor()))
//		{
//			return new Result<>(Boolean.FALSE, "专业只允许汉字、字母与数字的组合，最长为30个字符");
//		}
		return new Result<>(smtApplicationEducation.updateById());
	}

	/* (non-Javadoc)
	 * @see com.tce.smart.platform.service.SmtApplicationEducationService#getSmtApplicationEducationList(java.lang.String)
	 */
	@Override
	public List<EducationVO> getSmtApplicationEducationList(String applicationId) {
		// TODO Auto-generated method stub
		List<SmtApplicationEducation> selectList = mapper.selectList(Wrappers.<SmtApplicationEducation> query().lambda().eq(SmtApplicationEducation::getApplicationId, applicationId));
		Result<List<SysDict>> findByType = remoteDictService.findByType(DictConstants.EDUCATION_TYPE, SecurityConstants.FROM_IN);
		Result<List<SysDict>> findDegreeByType = remoteDictService.findByType(DictConstants.DEGREE_TYPE, SecurityConstants.FROM_IN);

		List<EducationVO> voList=new ArrayList<>();
		for (SmtApplicationEducation smt : selectList) {
			EducationVO  vo=new  EducationVO();
			vo.setId(smt.getId());
			vo.setEducationHisId(smt.getApplicationId().toString());
			vo.setSchoolName(smt.getSchoolName());
			vo.setDegree(smt.getDegree());
			vo.setEducation(smt.getEducation());
			vo.setMajor(smt.getMajor());
			vo.setStartTime(smt.getStartTime());
			vo.setEndTime(smt.getEndTime());
			vo.setIsHighDegreeType(smt.getIsHighDegreeType());
			vo.setIsHighEduType(smt.getIsHighEduType());
			if(findByType.getData().size()>0) {
				for (int j = 0; j < findByType.getData().size(); j++) {
					String value = findByType.getData().get(j).getValue();
					if(value.equals(smt.getEducation()))
					{
						vo.setEducationDesc(findByType.getData().get(j).getLabel());
						break;
					}
				}
			}
			if(findDegreeByType.getData().size()>0) {
				for (int j = 0; j < findDegreeByType.getData().size(); j++) {
					String value = findDegreeByType.getData().get(j).getValue();
					if(value.equals(smt.getDegree()))
					{
						vo.setDegreeDesc(findDegreeByType.getData().get(j).getLabel());
						break;
					}
				}
			}
			voList.add(vo);
		}
		return voList;
	}

	@Override
	public Integer deleteEducationList(String applicationId) {
		// TODO Auto-generated method stub
		return mapper.delete(Wrappers.<SmtApplicationEducation> query().lambda().eq(SmtApplicationEducation::getApplicationId, applicationId));
	}



}
