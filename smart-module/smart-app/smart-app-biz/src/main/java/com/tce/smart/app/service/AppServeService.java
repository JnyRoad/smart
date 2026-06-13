package com.tce.smart.app.service;

import java.util.List;

import com.tce.smart.app.dto.AppModuleDateDto;
import com.tce.smart.app.dto.AppModuleDto;
import com.tce.smart.app.dto.AppModuleInfoDto;
import com.tce.smart.app.dto.AppServerDto;
import com.tce.smart.app.entity.AppModuleInfo;
import com.tce.smart.app.vo.AppServeVo;

public interface AppServeService {
	List<AppModuleInfoDto> getAllMenu(Integer id);

	List<AppServeVo> getAllSreve();

	void  deleteById(int[] id);

	void  update(List<AppModuleInfoDto> list);

	void  addBatch(List<AppModuleInfoDto> list,int id);

	void  operate(AppServerDto appServerDto);

	void  updateName(AppModuleDateDto appModuleDateDto);

	void  add(List<AppModuleDto> appModuleDtos);

	Integer  addmodule(AppModuleDateDto appModuleDateDto);

	Integer  addParent(String name);

	AppServeVo getFix();

	Integer  addServeModule(AppModuleDateDto appModuleDateDto);

	/**
	 * 获取模块简单信息列表
	 *
	 * @return
	 */
	List<AppModuleInfo> getBusSimpleModule();
}
