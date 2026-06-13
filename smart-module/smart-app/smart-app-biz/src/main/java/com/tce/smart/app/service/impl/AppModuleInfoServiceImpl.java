package com.tce.smart.app.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.emun.DeleteState;
import com.tce.smart.app.emun.ModuleCatalog;
import com.tce.smart.app.entity.AppModuleInfo;
import com.tce.smart.app.mapper.AppModuleInfoMapper;
import com.tce.smart.app.service.AppModuleInfoService;

/**
 * 园区模块
 *
 * @author fushiping
 * @date 2019/5/21 0021 13:33
 **/
@Service
public class AppModuleInfoServiceImpl extends ServiceImpl<AppModuleInfoMapper, AppModuleInfo>
		implements AppModuleInfoService {

	@Override
	public Integer getIdByName(String name) {
		AppModuleInfo appModuleInfo = this.baseMapper
				.selectOne(Wrappers.<AppModuleInfo>query().lambda().eq(AppModuleInfo::getModuleName, name));
		return appModuleInfo.getId();
	}

	@Override
	public List<AppModuleInfo> getTopModule() {
		QueryWrapper<AppModuleInfo> queryWrapper = new QueryWrapper<AppModuleInfo>();
		queryWrapper.lambda()
				.eq(AppModuleInfo::getParentModule, ModuleCatalog.PARENT.getType())
				.eq(AppModuleInfo::getDelFlag, DeleteState.NORMOL.getCode());

		return this.baseMapper.selectList(queryWrapper);
	}

	@Override
	public List<AppModuleInfo> getTopModule(Integer catalogCode) {
		QueryWrapper<AppModuleInfo> queryWrapper = new QueryWrapper<AppModuleInfo>();
		queryWrapper.lambda()
				.eq(AppModuleInfo::getParentModule, ModuleCatalog.PARENT.getType())
				.eq(AppModuleInfo::getDelFlag, DeleteState.NORMOL.getCode())
				.eq(AppModuleInfo::getCatalogCode, catalogCode)
				.orderByAsc(AppModuleInfo::getModuleOrder);

		return this.baseMapper.selectList(queryWrapper);
	}

	@Override
	public List<AppModuleInfo> getSubModuleByPid(Integer parentId) {
		QueryWrapper<AppModuleInfo> queryWrapper = new QueryWrapper<AppModuleInfo>();
		queryWrapper.lambda()
			.eq(AppModuleInfo::getParentModule, parentId)
			.eq(AppModuleInfo::getDelFlag,DeleteState.NORMOL.getCode())
			.orderByAsc(AppModuleInfo::getModuleOrder);

		return this.baseMapper.selectList(queryWrapper);
	}

	@Override
	public List<AppModuleInfo> getSubModuleByIds(List<Integer> moduleIdList) {
		QueryWrapper<AppModuleInfo> queryWrapper = new QueryWrapper<AppModuleInfo>();
		queryWrapper.lambda().in(AppModuleInfo::getId, moduleIdList)
			.ne(AppModuleInfo::getParentModule, ModuleCatalog.PARENT.getType())
			.eq(AppModuleInfo::getDelFlag,DeleteState.NORMOL.getCode())
			.orderByAsc(AppModuleInfo::getModuleOrder);

		return this.baseMapper.selectList(queryWrapper);
	}

}
