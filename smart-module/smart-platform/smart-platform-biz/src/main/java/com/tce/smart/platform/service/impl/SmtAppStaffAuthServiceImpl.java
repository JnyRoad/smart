package com.tce.smart.platform.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.service.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.platform.core.ao.SmtAppStaffAuthBatchSaveAO;
import com.tce.smart.platform.core.ao.SmtAppStaffAuthSaveAO;
import com.tce.smart.platform.core.vo.StaffAuthModuleHrVO;
import com.tce.smart.platform.core.mapper.SmtAppStaffAuthMapper;
import com.tce.smart.tool.exception.TCEException;

import lombok.extern.slf4j.Slf4j;

/**
 * 员工App权限服务实现类
 *
 * @author mckaywu
 * @date 2019-06-12 11:23:20
 */
@Service
@Slf4j
public class SmtAppStaffAuthServiceImpl extends ServiceImpl<SmtAppStaffAuthMapper, SmtAppStaffAuth>
		implements SmtAppStaffAuthService {

	@Autowired
	private SmtAppAuthService smtAppAuthService;

	@Autowired
	private SmtStaffService smtStaffService;

	@Autowired
	private SmtAppHrAuthService smtAppHrAuthService;

	@Autowired
	private SmtParkBuService smtParkBuService;

	@Override
	public List<SmtAppStaffAuth> getStaffAuthList(Long staffId) {
		return this.list(Wrappers.<SmtAppStaffAuth>query().lambda().eq(SmtAppStaffAuth::getStaffId, staffId));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean addStaffAuth(SmtAppStaffAuthSaveAO appStaffAuthSaveAO) {
		if (ArrayUtils.isEmpty(appStaffAuthSaveAO.getAuthId())) {
			throw new TCEException("未分配权限");
		}
		SmtAppStaffAuth smtAppStaffAuth = null;
		for (int i = 0; i < appStaffAuthSaveAO.getAuthId().length; i++) {
			smtAppStaffAuth = new SmtAppStaffAuth();
			smtAppStaffAuth.setStaffId(Long.parseLong(appStaffAuthSaveAO.getStaffId()));
			smtAppStaffAuth.setAuthId(appStaffAuthSaveAO.getAuthId()[i]);
			smtAppStaffAuth.setCreate_time(DateUtils.date());

			// 保存
			this.save(smtAppStaffAuth);
		}

		return Boolean.TRUE;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean updateStaffAuth(SmtAppStaffAuthSaveAO appStaffAuthSaveAO) {
		// 删除旧数据
		this.deleteStaffAuth(Long.parseLong(appStaffAuthSaveAO.getStaffId()));

		// 添加新数据
		this.addStaffAuth(appStaffAuthSaveAO);

		return Boolean.TRUE;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean batchUpdateStaffAuth(SmtAppStaffAuthBatchSaveAO appStaffAuthBatchaveAO) {
		if (Objects.nonNull(appStaffAuthBatchaveAO) && ArrayUtils.isNotEmpty(appStaffAuthBatchaveAO.getStaffId())) {
			SmtAppStaffAuthSaveAO appStaffAuthSaveAO = null;
			for (int i = 0; i < appStaffAuthBatchaveAO.getStaffId().length; i++) {

				appStaffAuthSaveAO = new SmtAppStaffAuthSaveAO();
				appStaffAuthSaveAO.setStaffId(appStaffAuthBatchaveAO.getStaffId()[i]);
				appStaffAuthSaveAO.setAuthId(appStaffAuthBatchaveAO.getAuthId());

				this.updateStaffAuth(appStaffAuthSaveAO);
			}
		}

		return Boolean.TRUE;
	}

	@Override
	public Boolean deleteStaffAuth(Long staffId) {
		this.remove(Wrappers.<SmtAppStaffAuth>query().lambda().eq(SmtAppStaffAuth::getStaffId, staffId));
		return Boolean.TRUE;
	}

	@Override
	public List<String> getStaffModule(String badge) {
		if (StringUtils.isBlank(badge)) {
			return null;
		}
		List<String> moduleIdList = getAuthDetailIds(badge).getModuleId();
		return moduleIdList;
	}

	@Override
	public List<String> getStaffRecruitAuthLeve(String badge) {
		if (StringUtils.isBlank(badge)) {
			return null;
		}
		// 招聘职层ID集合
		List<String> recruitLeveList = null;
		StaffAuthModuleHrVO authModuleHrVO = getAuthDetailIds(badge);
		List<String> hrAuhtIdList = authModuleHrVO.getHrAuthId();
		if (CollectionUtils.isNotEmpty(hrAuhtIdList)) {
			// 招聘权限
			SmtAppHrAuth smtAppHrAuth = null;
			List<String[]> hrAuthArrList = new ArrayList<String[]>();
			for (String hrAuthId : hrAuhtIdList) {
				smtAppHrAuth = smtAppHrAuthService.getById(hrAuthId);
				if (Objects.nonNull(smtAppHrAuth)) {
					hrAuthArrList.add(smtAppHrAuth.getJobLeave().split(","));
				}
			}
			// 招聘职层ID集合
			recruitLeveList = getDistinctId(hrAuthArrList);
		}
		return recruitLeveList;
	}

	@Override
	public StaffAuthModuleHrVO getAuthDetailIds(String badge) {
		StaffAuthModuleHrVO staffAuthModuleHrVO = new StaffAuthModuleHrVO();

		// 查询员工信息
		SmtStaff smtStaff = smtStaffService.getSimpleSttaffByBadge(badge);
		if(Objects.isNull(smtStaff)) {
			throw new SmartException("员工为空");
		}
		Long staffId = smtStaff.getId();
		// 查询员工App权限
		QueryWrapper<SmtAppStaffAuth> queryWrapper = new QueryWrapper<SmtAppStaffAuth>();
		queryWrapper.lambda().eq(SmtAppStaffAuth::getStaffId, staffId);
		List<SmtAppStaffAuth> staffAuthList = this.baseMapper.selectList(queryWrapper);

		if (CollectionUtils.isNotEmpty(staffAuthList)) {
			// 模块ID数组
			List<String[]> moduleIdList = new ArrayList<String[]>(staffAuthList.size());
			// HR招聘权限职层ID数组
			List<String[]> hrAuthIdList = new ArrayList<String[]>(staffAuthList.size());
			SmtAppAuth smtAppAuth = null;
			for (SmtAppStaffAuth staffAuthTemp : staffAuthList) {
				// 查询权限
				smtAppAuth = smtAppAuthService.getById(staffAuthTemp.getAuthId());

				// 获取模块ID组
				if (StringUtils.isNotBlank(smtAppAuth.getModuleId())) {
					moduleIdList.add(smtAppAuth.getModuleId().split(","));
				}

				// 获取HR权限组
				if (StringUtils.isNotBlank(smtAppAuth.getHrAuthId())) {
					hrAuthIdList.add(smtAppAuth.getHrAuthId().split(","));
				}
			}

			// 设置员工号
			staffAuthModuleHrVO.setBragde(badge);
			// 模块ID去重
			staffAuthModuleHrVO.setModuleId(getDistinctId(moduleIdList));
			// HR招聘权限职层ID去重
			staffAuthModuleHrVO.setHrAuthId(getDistinctId(hrAuthIdList));
		}

		return staffAuthModuleHrVO;
	}

	@Override
	public Boolean initStaffAuth(Long staffId, Integer parkId) {
		SmtAppAuth smtAppAuth = smtAppAuthService.getInitAuth(parkId);
		if (Objects.nonNull(smtAppAuth)) {
			SmtAppStaffAuth smtAppStaffAuth = new SmtAppStaffAuth();
			smtAppStaffAuth.setStaffId(staffId);
			smtAppStaffAuth.setAuthId(smtAppAuth.getId());
			smtAppStaffAuth.setCreate_time(DateUtils.date());
			// 添加员工权限
			this.save(smtAppStaffAuth);
		}

		return Boolean.TRUE;
	}

	@Override
	public Boolean initLoginAuth(String badge) {

		SmtStaff smtStaff = smtStaffService.getSimpleSttaffByBadge(badge);
		if(Objects.isNull(smtStaff)) {
			log.info("为找到员工信息，工号=[{}]",badge);
			return Boolean.FALSE;
		}

		Integer authCount = this.count(Wrappers.<SmtAppStaffAuth>query()
				.lambda()
				.eq(SmtAppStaffAuth::getStaffId, smtStaff.getId()));

		if(authCount > 0) {
			log.info("员工已经分配,工号=[{}]",smtStaff.getId());
			return Boolean.TRUE;
		}
		if (StrUtil.isBlank(smtStaff.getCompId())) {
			log.info("员工BU不存在,员工ID={}",smtStaff.getId());
			return Boolean.FALSE;
		}
		List<SmtPark> parkList = smtParkBuService.getParkListByBu(Long.parseLong(smtStaff.getCompId()));
		if (CollUtil.isEmpty(parkList)) {
			log.info("员工BU未关联园区,员工ID={},{}",smtStaff.getId(), smtStaff.getCompId());
			return Boolean.FALSE;
		}
		this.initStaffAuth(smtStaff.getId(), parkList.get(0).getId());

		return Boolean.TRUE;
	}

	/**
	 * 获取模块ID集合
	 *
	 * @param parmsList 模块ID数组集合
	 * @return 去重后的ID集合
	 */
	private List<String> getDistinctId(List<String[]> parmsList) {
		List<String> distinctIdList = null;
		if (CollectionUtils.isEmpty(parmsList)) {
			return null;
		}

		distinctIdList = new ArrayList<String>();
		for (String[] moduleArray : parmsList) {
			distinctIdList.addAll(Arrays.asList(moduleArray));
		}

		distinctIdList = distinctIdList.stream().distinct().collect(Collectors.toList());

		return distinctIdList;
	}
}
