package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.ao.SmtAppAuthSaveAO;
import com.tce.smart.platform.core.entity.SmtAppAuth;
import com.tce.smart.platform.core.mapper.SmtAppAuthMapper;
import com.tce.smart.platform.service.SmtAppAuthService;
import com.tce.smart.tool.enums.AppAuthInitFlagnum;
import com.tce.smart.tool.enums.DeleteStatusEnum;
import com.tce.smart.tool.exception.TCEException;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * App权限服务实现类
 *
 * @author mckaywu
 * @date 2019-06-12 11:22:24
 */
@Service
public class SmtAppAuthServiceImpl extends ServiceImpl<SmtAppAuthMapper, SmtAppAuth> implements SmtAppAuthService {

	@Override
	public IPage<SmtAppAuth> getSmtAuthPage(Page<SmtAppAuth> page, SmtAppAuth smtAppAuth) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		if (Objects.nonNull(smtAppAuth.getParkId())) {
			parkIdList = new ArrayList<>(1);
			parkIdList.add(smtAppAuth.getParkId());
		}
		QueryWrapper<SmtAppAuth> queryWrapper = new QueryWrapper<SmtAppAuth>();
		queryWrapper.lambda().eq(SmtAppAuth::getDelFlag, DeleteStatusEnum.NOT_DELETE.getCode())
				.in(SmtAppAuth::getParkId, parkIdList)
				.like(StringUtils.isNotBlank(smtAppAuth.getAuthName()),
						SmtAppAuth::getAuthName, smtAppAuth.getAuthName())
				.orderByDesc(SmtAppAuth::getCreateTime);
		return baseMapper.selectPage(page, queryWrapper);
	}

	@Override
	public List<SmtAppAuth> getAuthList() {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		QueryWrapper<SmtAppAuth> queryWrapper = new QueryWrapper<SmtAppAuth>();
		queryWrapper.lambda().eq(SmtAppAuth::getDelFlag, DeleteStatusEnum.NOT_DELETE.getCode()).in(SmtAppAuth::getParkId, parkIdList);
		return baseMapper.selectList(queryWrapper);
	}

	@Override
	public Boolean addAuth(SmtAppAuthSaveAO appAuthSaveAO) {

		SmtAppAuth smtAppAuth = new SmtAppAuth();
		smtAppAuth.setAuthName(appAuthSaveAO.getAuthName());

		smtAppAuth.setModuleId(StringUtils.join(appAuthSaveAO.getModuleId(), ","));
		smtAppAuth.setJcheId(StringUtils.join(appAuthSaveAO.getJcheId(), ","));

		// HR招聘数据查看权限
		if (ArrayUtils.isNotEmpty(appAuthSaveAO.getHrAuthId())) {
			smtAppAuth.setHrAuthId(StringUtils.join(appAuthSaveAO.getHrAuthId(), ","));
		}

		smtAppAuth.setAuthDesc(appAuthSaveAO.getAuthDesc());
		smtAppAuth.setDelFlag(DeleteStatusEnum.NOT_DELETE.getCode());
		if (appAuthSaveAO.getInitFlag() != null) {
			Boolean isExist = getInitFlag(appAuthSaveAO.getParkId());
			if (isExist && AppAuthInitFlagnum.INIT.getCode().equals(appAuthSaveAO.getInitFlag())) {
				throw new SmartException("该园区已存在通用权限");
			}
			smtAppAuth.setInitFlag(appAuthSaveAO.getInitFlag());
		} else {
			smtAppAuth.setInitFlag(AppAuthInitFlagnum.ADD_NEW.getCode());
		}
		smtAppAuth.setCreateTime(DateUtils.date());
		smtAppAuth.setParkId(appAuthSaveAO.getParkId());

		this.save(smtAppAuth);

		return Boolean.TRUE;
	}

	@Override
	public Boolean updateAuthById(SmtAppAuthSaveAO appAuthSaveAO) {
		if (Objects.isNull(appAuthSaveAO.getId())) {
			throw new TCEException("修改操作ID不能为空");
		}

		SmtAppAuth smtAppAuth = new SmtAppAuth();

		smtAppAuth.setId(appAuthSaveAO.getId());
		smtAppAuth.setAuthName(appAuthSaveAO.getAuthName());

		smtAppAuth.setModuleId(StringUtils.join(appAuthSaveAO.getModuleId(), ","));
		smtAppAuth.setJcheId(StringUtils.join(appAuthSaveAO.getJcheId(), ","));

		// HR招聘数据查看权限
		if (ArrayUtils.isNotEmpty(appAuthSaveAO.getHrAuthId())) {
			smtAppAuth.setHrAuthId(StringUtils.join(appAuthSaveAO.getHrAuthId(), ","));
		}
		if (appAuthSaveAO.getInitFlag() != null) {
			smtAppAuth.setInitFlag(appAuthSaveAO.getInitFlag());
		}
		smtAppAuth.setParkId(appAuthSaveAO.getParkId());
		smtAppAuth.setAuthDesc(appAuthSaveAO.getAuthDesc());

		this.updateById(smtAppAuth);

		return Boolean.TRUE;
	}

	@Override
	public Boolean removeAuthById(Integer id) {
		if (Objects.isNull(id)) {
			throw new TCEException("删除操作ID不能为空");
		}

		SmtAppAuth smtAppAuth = new SmtAppAuth();

		smtAppAuth.setId(id);
		smtAppAuth.setDelFlag(DeleteStatusEnum.IS_DELETE.getCode());

		this.updateById(smtAppAuth);

		return Boolean.TRUE;
	}

	@Override
	public SmtAppAuth getInitAuth() {
		List<SmtAppAuth> smtAppAuthList = this.list(
				Wrappers.<SmtAppAuth>query()
				.lambda()
				.eq(SmtAppAuth::getInitFlag, AppAuthInitFlagnum.INIT.getCode()));

		if (CollectionUtils.isEmpty(smtAppAuthList) || smtAppAuthList.size() > 1) {
			throw new TCEException("未找到初始权限，或配置了多条初始权限");
		}

		return smtAppAuthList.get(0);
	}

	@Override
	public SmtAppAuth getInitAuth(Integer parkId) {
		List<SmtAppAuth> smtAppAuthList = this.list(
				Wrappers.<SmtAppAuth>query().lambda()
						.eq(SmtAppAuth::getParkId, parkId)
						.eq(SmtAppAuth::getInitFlag, AppAuthInitFlagnum.INIT.getCode()));

		if (CollUtil.isEmpty(smtAppAuthList)) {
			throw new TCEException("未找到初始权限");
		}

		return smtAppAuthList.get(0);
	}

	@Override
	public Boolean getInitFlag(Integer parkId) {
		SmtAppAuth smtAppAuth = this.getOne(
				Wrappers.<SmtAppAuth>query().lambda()
						.eq(SmtAppAuth::getParkId, parkId)
						.eq(SmtAppAuth::getInitFlag, AppAuthInitFlagnum.INIT.getCode()));
		return smtAppAuth != null && AppAuthInitFlagnum.INIT.getCode().equals(smtAppAuth.getInitFlag());
	}
}
