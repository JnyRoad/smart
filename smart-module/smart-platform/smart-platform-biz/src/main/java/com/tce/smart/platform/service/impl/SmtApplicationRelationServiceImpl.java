package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.RegexUtils;
import com.tce.smart.platform.core.entity.SmtApplicationRelation;
import com.tce.smart.platform.core.vo.OrgrelationVO;
import com.tce.smart.platform.core.mapper.SmtApplicationRelationMapper;
import com.tce.smart.platform.service.SmtApplicationRelationService;
import com.tce.smart.tool.constant.DictConstants;

import lombok.AllArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 应聘者人际关系表
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:26
 */
@AllArgsConstructor
@Service
@Slf4j
public class SmtApplicationRelationServiceImpl extends ServiceImpl<SmtApplicationRelationMapper, SmtApplicationRelation> implements SmtApplicationRelationService {


	private final SmtApplicationRelationMapper mapper;

	@Autowired
	private RemoteDictService remoteDictService;
	@Override
	public Result addApplicationRelation(SmtApplicationRelation smtApplicationRelation) {
		// TODO Auto-generated method stub
		if(!RegexUtils.matchName(smtApplicationRelation.getRelation()))
		{
			return new Result<>(Boolean.FALSE, "人事关系只允许汉字、字母与数字的组合，最长为30个字符");
		}
		if(!RegexUtils.matchName(smtApplicationRelation.getName()))
		{
			return new Result<>(Boolean.FALSE, "联系人姓名只允许汉字、字母与数字的组合，最长为30个字符");
		}
		return new Result<>(smtApplicationRelation.insert());
	}

	@Override
	public Result updateApplicationRelation(SmtApplicationRelation smtApplicationRelation) {
		// TODO Auto-generated method stub
		if(!RegexUtils.matchName(smtApplicationRelation.getRelation()))
		{
			return new Result<>(Boolean.FALSE, "人事关系只允许汉字、字母与数字的组合，最长为30个字符");
		}
		if(!RegexUtils.matchName(smtApplicationRelation.getName()))
		{
			return new Result<>(Boolean.FALSE, "联系人姓名只允许汉字、字母与数字的组合，最长为30个字符");
		}
		return new Result<>(this.updateById(smtApplicationRelation));
	}

	@Override
	public Result getByApplicationId(String applicationId) {
		log.info("获取应聘者关系ApplicationId:{}",applicationId);
		return Result.builder().data(mapper.selectByApplicationId(applicationId)).build();
	}





	@Override
	public Result removeRelationByApplicationId(Long applicationId) {
		// TODO Auto-generated method stub
		boolean delete = this.remove(Wrappers.<SmtApplicationRelation> query().lambda().eq(SmtApplicationRelation::getApplicationId, applicationId));
		 return new Result<>(delete);
	}

	@Override
	public List<OrgrelationVO> getApplicationInfo(String applicationId) {
		// TODO Auto-generated method stub
		Result<List<SysDict>> findRelationByType = remoteDictService.findByType(DictConstants.REALTION_TYPE, SecurityConstants.FROM_IN);
		List<SmtApplicationRelation> relations= this.list(Wrappers.<SmtApplicationRelation> query().lambda().eq(SmtApplicationRelation::getApplicationId, applicationId));
		List<OrgrelationVO> relationList=new ArrayList<>();
		for (SmtApplicationRelation re : relations) {
			OrgrelationVO vo=new OrgrelationVO();
			vo.setOrgPersonBu(re.getCompName());
			vo.setOrgPersonDept(re.getDeptName());
			vo.setOrgPersonGender(re.getSex());
			vo.setRelationType(Integer.parseInt(re.getRelation()));
			vo.setRelationTypeDesc("");
			vo.setOrgPersonSection(re.getClassName());
			vo.setOrgPersonName(re.getName());
			vo.setJobName(re.getJobName());
			vo.setRelationDetail(re.getRelationDetail());
			vo.setBadege(re.getBadge());
			vo.setOrgrelationId(re.getId());
			if(findRelationByType.getData().size()>0) {
				for (int j = 0; j < findRelationByType.getData().size(); j++) {
					String value = findRelationByType.getData().get(j).getValue();
					if(value.equals(re.getRelation()))
					{
						vo.setRelationTypeDesc(findRelationByType.getData().get(j).getLabel());
						break;
					}
				}
			}
			relationList.add(vo);
		}
		return relationList;
	}


}
