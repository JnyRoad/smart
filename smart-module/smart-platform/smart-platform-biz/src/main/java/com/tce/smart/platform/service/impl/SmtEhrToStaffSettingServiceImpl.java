package com.tce.smart.platform.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tce.smart.tool.constant.SymbolConstants;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.dto.AddEhrToStaffSettingDTO;
import com.tce.smart.platform.core.entity.SmtEhrToStaffSetting;
import com.tce.smart.platform.core.mapper.SmtEhrToStaffSettingMapper;
import com.tce.smart.platform.service.SmtEhrToStaffSettingService;

import cn.hutool.core.util.ObjectUtil;

@Service
public class SmtEhrToStaffSettingServiceImpl extends ServiceImpl<SmtEhrToStaffSettingMapper, SmtEhrToStaffSetting> implements SmtEhrToStaffSettingService {

	@Override
	public Boolean addList(AddEhrToStaffSettingDTO dto) {
		// TODO Auto-generated method stub

		if(dto.getCompIds().size()>0)
		{
			List<SmtEhrToStaffSetting> list = this.list();
			for (SmtEhrToStaffSetting smtEhrToStaffSetting : list) {
				this.removeById(smtEhrToStaffSetting.getId());
			}
			List<String> compIds = dto.getCompIds();
			for (String string : compIds) {
				SmtEhrToStaffSetting set=new SmtEhrToStaffSetting();
				set.setCompId(string);
				set.setTime(dto.getTime());
				set.setTimeUnit(dto.getTimeUnit());
				set.setCreateTime(LocalDateTime.now());
				if(ObjectUtil.isNotNull(SecurityUtils.getUser()))
				{
					set.setCreateUser(SecurityUtils.getUser().getUsername());
				}
				if(dto.getTimeUnit().equals("时"))
				{
					set.setTimeSecond(dto.getTime()*60*60);
				}
				if(dto.getTimeUnit().equals("分"))
				{
					set.setTimeSecond(dto.getTime()*60);
				}
				if(dto.getTimeUnit().equals("日"))
				{
					set.setTimeSecond(dto.getTime()*60*60*24);
				}
				if(dto.getTimeUnit().equals("周"))
				{
					set.setTimeSecond(dto.getTime()*60*60*24*7);
				}
				if(dto.getTimeUnit().equals("月"))
				{
					set.setTimeSecond(dto.getTime()*60*60*24*30);
				}
				set.insert();

			}
		}
		return true;
	}

	@Override
	public List<SmtEhrToStaffSetting> getListEHR() {
		return this.list(new LambdaQueryWrapper<SmtEhrToStaffSetting>().eq(SmtEhrToStaffSetting::getCreateUser,SymbolConstants.EHR_STR));
	}

	@Override
	public Boolean addListEHR(AddEhrToStaffSettingDTO dto) {
		return saveBySource(dto, SymbolConstants.EHR_STR);
	}

	@Override
	public List<SmtEhrToStaffSetting> getListDHR() {
		return this.list(new LambdaQueryWrapper<SmtEhrToStaffSetting>().eq(SmtEhrToStaffSetting::getCreateUser,SymbolConstants.DHR_STR));
	}

	@Override
	public Boolean addListDHR(AddEhrToStaffSettingDTO dto) {
		return saveBySource(dto,SymbolConstants.DHR_STR);
	}


	private Boolean saveBySource(AddEhrToStaffSettingDTO dto,final String source){
		if(dto.getCompIds().size()>0)
		{
			List<SmtEhrToStaffSetting> list = this.list(new LambdaQueryWrapper<SmtEhrToStaffSetting>().eq(SmtEhrToStaffSetting::getCreateUser,source));
			List<Integer> idList = list.stream().map(SmtEhrToStaffSetting::getId).collect(Collectors.toList());
			if(CollectionUtil.isNotEmpty(idList)){
				this.removeByIds(idList);
			}
			List<String> compIds = dto.getCompIds();
			List<SmtEhrToStaffSetting> addList = new ArrayList<>();
			for (String string : compIds) {
				SmtEhrToStaffSetting set=new SmtEhrToStaffSetting();
				set.setCompId(string);
				set.setTime(dto.getTime());
				set.setTimeUnit(dto.getTimeUnit());
				set.setCreateTime(LocalDateTime.now());
				set.setCreateUser(source);
				if(dto.getTimeUnit().equals("时"))
				{
					set.setTimeSecond(dto.getTime()*60*60);
				}
				if(dto.getTimeUnit().equals("分"))
				{
					set.setTimeSecond(dto.getTime()*60);
				}
				if(dto.getTimeUnit().equals("日"))
				{
					set.setTimeSecond(dto.getTime()*60*60*24);
				}
				if(dto.getTimeUnit().equals("周"))
				{
					set.setTimeSecond(dto.getTime()*60*60*24*7);
				}
				if(dto.getTimeUnit().equals("月"))
				{
					set.setTimeSecond(dto.getTime()*60*60*24*30);
				}
				addList.add(set);
			}

			if(CollectionUtil.isNotEmpty(addList)){
				this.saveBatch(addList);
			}
		}
		return true;
	}
}
