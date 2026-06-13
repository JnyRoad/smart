package com.tce.smart.app.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.dto.AppModuleDateDto;
import com.tce.smart.app.dto.AppModuleDto;
import com.tce.smart.app.dto.AppModuleInfoDto;
import com.tce.smart.app.dto.AppServerDto;
import com.tce.smart.app.emun.DeleteState;
import com.tce.smart.app.emun.ModuleCatalog;
import com.tce.smart.app.entity.AppModuleInfo;
import com.tce.smart.app.entity.AppSubjectModule;
import com.tce.smart.app.mapper.AppModuleInfoMapper;
import com.tce.smart.app.mapper.AppSubjectModuleMapper;
import com.tce.smart.app.service.AppAgreeService;
import com.tce.smart.app.service.AppModuleInfoService;
import com.tce.smart.app.service.AppServeService;
import com.tce.smart.app.vo.AppServeVo;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.tool.enums.ExceptionTypeEnum;
import com.tce.smart.tool.exception.TCEException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AppServeServiceImpl extends ServiceImpl<AppModuleInfoMapper, AppModuleInfo> implements AppServeService {
	@Autowired
	private AppModuleInfoMapper appModuleInfoMapper;

	@Autowired
	private AppSubjectModuleMapper appSubjectModuleMapper;

	@Autowired
	private AppAgreeService appAgreeService;

	@Autowired
	private AppModuleInfoService appModuleInfoService;

	/**
	 * 批量删除
	 *
	 * @param id
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteById(int[] id) {
		this.checkArray(id);
		for (int j : id) {
			AppModuleInfo appModuleInfo = this.getById(j);
			if (appModuleInfo.getParentModule() != 0) {
				appModuleInfo.setDelFlag(DeleteState.DELETE.getCode());
				this.updateById(appModuleInfo);
			} else {
				this.deleteServer(j);
			}
		}
	}

	/**
	 * 获取所有顶级模块
	 *
	 * @return
	 */
	@Override
	public List<AppServeVo> getAllSreve() {
		Integer severId = this.getFix().getId();
		List<AppModuleInfo> list = appModuleInfoMapper.selectList(Wrappers.<AppModuleInfo>query().lambda()
				.eq(AppModuleInfo::getParentModule, ModuleCatalog.PARENT.getType())
				.eq(AppModuleInfo::getDelFlag, DeleteState.NORMOL.getCode())
				.ne(AppModuleInfo::getId, severId)
		);
		List<AppServeVo> vo = new ArrayList<AppServeVo>();
		list.forEach(AppModuleInfo -> {
			AppServeVo appServeVo = new AppServeVo();
			appServeVo.setId(AppModuleInfo.getId());
			appServeVo.setModuleName(AppModuleInfo.getModuleName());
			appServeVo.setEditStatus(false);
			appServeVo.setModuleStatus(false);
			List<AppModuleInfoDto> appModule = this.getAllMenu(AppModuleInfo.getId());
			appServeVo.setModule(appModule);
			vo.add(appServeVo);
		});
		return vo;
	}

	/**
	 * 根据顶级模块获取子模块
	 *
	 * @param id
	 * @return
	 */
	@Override
	public List<AppModuleInfoDto> getAllMenu(Integer id) {
		List<AppModuleInfo> list = appModuleInfoMapper.selectList(Wrappers.<AppModuleInfo>query().lambda()
				.eq(AppModuleInfo::getParentModule, id)
				.eq(AppModuleInfo::getDelFlag, DeleteState.NORMOL.getCode()));
		List<AppModuleInfoDto> appModuleInfoDtos = new ArrayList<>();
		list.forEach(AppModuleInfo -> {
			AppModuleInfoDto appModuleInfoDto = new AppModuleInfoDto();
			BeanUtils.copyProperties(AppModuleInfo, appModuleInfoDto);
			if (AppModuleInfo.getModuleIcon() != null) {
				String s = new String(AppModuleInfo.getModuleIcon());
				//appModuleInfoDto.setModuleIcon(new String(AppModuleInfo.getModuleIcon()));
				appModuleInfoDto.setModuleIcon(s);
			}
			appModuleInfoDto.setInEdit(false);
			appModuleInfoDto.setNameEditAble(false);
			appModuleInfoDtos.add(appModuleInfoDto);
		});
		return appModuleInfoDtos;
	}

	/**
	 * 批量更新业务经营模块
	 *
	 * @param list
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void update(List<AppModuleInfoDto> list) {
		list.forEach(AppModuleInfoDto -> {
			if (AppModuleInfoDto.getId() != null) {
				this.checkDto(AppModuleInfoDto);
				AppModuleInfo appModuleInfo = this.getById(AppModuleInfoDto.getId());
				appModuleInfo.setModuleName(AppModuleInfoDto.getModuleName());
                appModuleInfo.setModuleIcon(AppModuleInfoDto.getModuleIcon().getBytes(StandardCharsets.UTF_8));
                appModuleInfo.setModuleUrl(AppModuleInfoDto.getModuleUrl());
				appModuleInfo.setUpdateTime(LocalDateTime.now());
				this.updateById(appModuleInfo);
			}
		});

	}

	@Override
	public List<AppModuleInfo> getBusSimpleModule() {
		// 查询业务顶级模块
		List<AppModuleInfo> businessModuleList = appModuleInfoService.getTopModule(ModuleCatalog.BISINE.getType());
		if (CollectionUtils.isEmpty(businessModuleList)) {
			throw new TCEException("查询业务模块信息异常");
		} else if (businessModuleList.size() > 1) {// 只会有一个业务模块集合
			throw new TCEException("业务模块配置异常");
		}

		// 查询自定义子模块

		return appModuleInfoService.getSubModuleByPid(businessModuleList.get(0).getId());
	}

	/**
	 * 删除顶级模块
	 *
	 * @param id
	 */
	private void deleteServer(int id) {
		List<AppModuleInfoDto> list = this.getAllMenu(id);
		list.forEach(AppModuleInfoDto -> {
			AppModuleInfo appModuleInfo = this.getById(AppModuleInfoDto.getId());
			appModuleInfo.setDelFlag(DeleteState.DELETE.getCode());
			this.updateById(appModuleInfo);
		});
		AppModuleInfo appModuleInfo = this.getById(id);
		appModuleInfo.setDelFlag(DeleteState.DELETE.getCode());
		this.updateById(appModuleInfo);
		this.deleteAgree(id);
	}

	/**
	 * 删除顶级模块时删除对应的协议
	 *
	 * @param id
	 */
	private void deleteAgree(int id) {
		List<AppSubjectModule> list = appSubjectModuleMapper.selectList(Wrappers
				.<AppSubjectModule>query().lambda()
				.eq(AppSubjectModule::getModuleId, id)
		);
		list.forEach(AppSubjectModule -> appAgreeService.deleteAgree(AppSubjectModule.getSubjectId()));
	}

	/**
	 * 添加顶级模块下子模块
	 *
	 * @param list
	 * @param id
	 */
	@Override
	public void addBatch(List<AppModuleInfoDto> list, int id) {
		list.forEach(AppModuleInfoDto -> {
			this.checkDto(AppModuleInfoDto);
			AppModuleInfo appModuleInfo = new AppModuleInfo();
			appModuleInfo.setModuleName(AppModuleInfoDto.getModuleName());
			appModuleInfo.setModuleUrl(AppModuleInfoDto.getModuleUrl());
            appModuleInfo.setModuleIcon(AppModuleInfoDto.getModuleIcon().getBytes(StandardCharsets.UTF_8));
            appModuleInfo.setDelFlag(DeleteState.NORMOL.getCode());
			appModuleInfo.setParentModule(id);
			appModuleInfo.setCreateTime(LocalDateTime.now());
			appModuleInfoMapper.insert(appModuleInfo);
		});
	}


	/**
	 * 添加顶级模块并返回ID
	 *
	 * @param appModuleInfo
	 * @return
	 */
	public Integer addServe(AppModuleInfo appModuleInfo) {
		this.checkName(appModuleInfo.getModuleName());
		appModuleInfo.setCreateTime(LocalDateTime.now());
		appModuleInfo.setParentModule(ModuleCatalog.PARENT.getType());
		appModuleInfo.setDelFlag(DeleteState.NORMOL.getCode());
		appModuleInfoMapper.insert(appModuleInfo);
		return appModuleInfo.getId();
	}

	/**
	 * 根据获取数据判断将要进行的各种操作
	 *
	 * @param appServerDto
	 */
	@Override
	public void operate(AppServerDto appServerDto) {
		this.deleteById(appServerDto.getId());
		for (int i = 0; i < appServerDto.getInsertData().size(); i++) {
			this.addtest(appServerDto.getInsertData().get(i));
		}
	}

	/**
	 * 添加数据
	 *
	 * @param appModuleDtos
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void add(List<AppModuleDto> appModuleDtos) {
		if (appModuleDtos.size() > 0) {
			appModuleDtos.forEach(this::addtest);
		}
	}

	/**
	 * 获取业务模块
	 *
	 * @return
	 */
	@Override
	public AppServeVo getFix() {
		AppModuleInfo appModuleInfo = appModuleInfoMapper.selectOne(Wrappers.<AppModuleInfo>query().lambda()
				.eq(AppModuleInfo::getParentModule, ModuleCatalog.PARENT.getType())
				.eq(AppModuleInfo::getDelFlag, DeleteState.NORMOL.getCode())
				.eq(AppModuleInfo::getCatalogCode, ModuleCatalog.BISINE.getType() + ""));
		if (appModuleInfo == null) {
			Integer i = this.addServerParent(ModuleCatalog.BISINE.getDesc());
			appModuleInfo = appModuleInfoMapper.selectById(i);
		}
		AppServeVo appServeVo = new AppServeVo();
		appServeVo.setId(appModuleInfo.getId());
		appServeVo.setModuleName(appModuleInfo.getModuleName());
		appServeVo.setEditStatus(false);
		appServeVo.setModuleStatus(false);
		appServeVo.setModule(this.getAllMenu(appModuleInfo.getId()));
		return appServeVo;
	}

	/**
	 * 单个添加非业务二级模块信息
	 *
	 * @param appModuleDateDto
	 */
	@Override
	public Integer addmodule(AppModuleDateDto appModuleDateDto) {
		this.checkInsertDate(appModuleDateDto);
		AppModuleInfo appModuleInfo = new AppModuleInfo();
		BeanUtils.copyProperties(appModuleDateDto, appModuleInfo);
        appModuleInfo.setModuleIcon(appModuleDateDto.getModuleIcon().getBytes(StandardCharsets.UTF_8));
        appModuleInfo.setDelFlag(DeleteState.NORMOL.getCode());
		appModuleInfo.setCatalogCode(ModuleCatalog.CUSTOM.getType());
		appModuleInfo.setCreateTime(LocalDateTime.now());
		appModuleInfoMapper.insert(appModuleInfo);
		return appModuleInfo.getId();
	}

	/**
	 * 单个添加业务二级模块信息
	 *
	 * @param appModuleDateDto
	 */
	@Override
	public Integer addServeModule(AppModuleDateDto appModuleDateDto) {
		this.checkInsertDate(appModuleDateDto);
		AppModuleInfo appModuleInfo = new AppModuleInfo();
		BeanUtils.copyProperties(appModuleDateDto, appModuleInfo);
        appModuleInfo.setModuleIcon(appModuleDateDto.getModuleIcon().getBytes(StandardCharsets.UTF_8));
        appModuleInfo.setDelFlag(DeleteState.NORMOL.getCode());
		appModuleInfo.setCatalogCode(ModuleCatalog.BISINE.getType());
		appModuleInfo.setCreateTime(LocalDateTime.now());
		appModuleInfoMapper.insert(appModuleInfo);
		return appModuleInfo.getId();
	}

	/**
	 * 添加顶级模块信息
	 *
	 * @param name
	 * @return
	 */
	@Override
	public Integer addParent(String name) {
		this.checkName(name);
		AppModuleInfo appModuleInfo = new AppModuleInfo();
		appModuleInfo.setModuleName(name);
		appModuleInfo.setCreateTime(LocalDateTime.now());
		appModuleInfo.setParentModule(ModuleCatalog.PARENT.getType());
		appModuleInfo.setCatalogCode(ModuleCatalog.CUSTOM.getType());
		appModuleInfo.setDelFlag(DeleteState.NORMOL.getCode());
		appModuleInfoMapper.insert(appModuleInfo);
		return appModuleInfo.getId();
	}

	/**
	 * 更新一级模块名称
	 *
	 * @param appModuleDateDto
	 */
	@Override
	public void updateName(AppModuleDateDto appModuleDateDto) {
		AppModuleInfo appModuleInfo = this.getById(appModuleDateDto.getId());
		this.checkName(appModuleDateDto.getModuleName());
		appModuleInfo.setModuleName(appModuleDateDto.getModuleName());
		appModuleInfo.setUpdateTime(LocalDateTime.now());
		this.updateById(appModuleInfo);
	}

	/**
	 * 添加业务模块信息
	 *
	 * @param name
	 * @return
	 */
	private Integer addServerParent(String name) {
		this.checkName(name);
		AppModuleInfo appModuleInfo = new AppModuleInfo();
		appModuleInfo.setModuleName(name);
		appModuleInfo.setCreateTime(LocalDateTime.now());
		appModuleInfo.setParentModule(ModuleCatalog.PARENT.getType());
		appModuleInfo.setCatalogCode(ModuleCatalog.BISINE.getType());
		appModuleInfo.setDelFlag(DeleteState.NORMOL.getCode());
		appModuleInfoMapper.insert(appModuleInfo);
		return appModuleInfo.getId();
	}

	private void addtest(AppModuleDto appModuleDto) {
		Integer i = this.addServe(appModuleDto.getAppParent());
		this.addBatch(appModuleDto.getListChild(), i);
	}

	/**
	 * 检查删除数组
	 *
	 * @param ids
	 */
	private void checkArray(int[] ids) {
		if (Array.getLength(ids) == 0) {
			throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_BATCH_NULL);
		}
	}

	/**
	 * 检验更新输入数据的格式
	 *
	 * @param appModuleInfoDto
	 */
	private void checkDto(AppModuleInfoDto appModuleInfoDto) {
		if (StringUtils.isBlank(appModuleInfoDto.getModuleName())) {
			throw new TCEException(ExceptionTypeEnum.APP_MODULE_NAME_NULL);
		}
		if (appModuleInfoDto.getModuleIcon() == null) {
			throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_PICTURE_NULL);
		}
		if (appModuleInfoDto.getModuleUrl().trim().length() == 0 || appModuleInfoDto.getModuleUrl() == null) {
			throw new TCEException(ExceptionTypeEnum.APP_MODULE_URL_NULL);
		}
	}

	/**
	 * 检验插入数据的格式
	 *
	 * @param appModuleDateDto
	 */
	private void checkInsertDate(AppModuleDateDto appModuleDateDto) {
		if (StringUtils.isBlank(appModuleDateDto.getModuleName())) {
			throw new TCEException(ExceptionTypeEnum.APP_MODULE_NAME_NULL);
		}
		if (appModuleDateDto.getModuleIcon() == null) {
			throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_PICTURE_NULL);
		}
		if (appModuleDateDto.getModuleUrl().trim().length() == 0 || appModuleDateDto.getModuleUrl() == null) {
			throw new TCEException(ExceptionTypeEnum.APP_MODULE_URL_NULL);
		}
	}

	/**
	 * 检验添加名字格式
	 *
	 * @param name
	 */
	private void checkName(String name) {
		if (StringUtils.isBlank(name)) {
			throw new TCEException(ExceptionTypeEnum.APP_MODULE_NAME_NULL);
		}
		int count = appModuleInfoMapper.selectCount(Wrappers.<AppModuleInfo>query().lambda()
				.eq(AppModuleInfo::getParentModule, ModuleCatalog.PARENT.getType())
				.eq(AppModuleInfo::getDelFlag, DeleteState.NORMOL.getCode())
				.eq(AppModuleInfo::getModuleName, name)
		);
		if (count > 0) {
			throw new TCEException(ExceptionTypeEnum.APP_MODULE_NAME_EXIST);
		}
	}

}
