package com.tce.smart.app.service.impl;

import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.emun.AdverPositionEnum;
import com.tce.smart.app.emun.DeleteState;
import com.tce.smart.app.emun.PublishState;
import com.tce.smart.app.entity.AppAdverInfo;
import com.tce.smart.app.mapper.AppAdverInfoMapper;
import com.tce.smart.app.service.AppAdverInfoService;
import com.tce.smart.tool.exception.TCEException;
import io.netty.util.internal.StringUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/***
 * description: app广告管理服务实现类 <br>
 * date: 2019/12/30 17:52 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@Service
public class AppAdverInfoServiceImpl extends ServiceImpl<AppAdverInfoMapper, AppAdverInfo> implements AppAdverInfoService {

	@Override
	public IPage<AppAdverInfo> listByPage(Page page, AppAdverInfo appAdverInfo) {
		return this.page(page, Wrappers.<AppAdverInfo>query().lambda().eq(AppAdverInfo::getDelFlag,
				DeleteState.NORMOL.getCode()));
	}

	@Override
	public Boolean saveAdver(AppAdverInfo saveAo) {
		//字段校验
		checkInsertAo(saveAo);
		return this.save(saveAo);
	}

	@Override
	public Boolean updateAdverById(AppAdverInfo saveAo) {
		//字段校验
		checkInsertAo(saveAo);
		return this.updateById(saveAo);
	}

	@Override
	public Boolean publish(Integer id) {
		AppAdverInfo saveBean = new AppAdverInfo();
		saveBean.setId(id);
		saveBean.setPublishFlag(PublishState.ONLINE.getCode());
		return this.updateById(saveBean);
//		return this.update(Wrappers.<AppAdverInfo>update().lambda().eq(AppAdverInfo::getId, id).set
//		(AppAdverInfo::getPublishFlag, PublishState.ONLINE.getCode()));
	}

	@Override
	public Boolean unpPublish(Integer id) {
		AppAdverInfo saveBean = new AppAdverInfo();
		saveBean.setId(id);
		saveBean.setPublishFlag(PublishState.ONLINE.getCode());
		return this.updateById(saveBean);
//		return this.update(Wrappers.u().eq(AppAdverInfo::getId, id).set(AppAdverInfo::getPublishFlag, PublishState
//		.INIT.getCode()));
	}

	@Override
	public Boolean deleteAdver(Integer id) {
		AppAdverInfo saveBean = new AppAdverInfo();
		saveBean.setId(id);
		saveBean.setDelFlag(DeleteState.DELETE.getCode());
		return this.updateById(saveBean);
	}

	@Override
	public List<AppAdverInfo> getAdverByPosition(Integer adverPosition) {
		return this.list(Wrappers.<AppAdverInfo>query().lambda()
				.eq(AppAdverInfo::getImagePosition,adverPosition)
				.eq(AppAdverInfo::getDelFlag,DeleteState.NORMOL.getCode()));
	}

	private void checkInsertAo(AppAdverInfo saveAo) {
		if (Objects.isNull(saveAo)) {
			throw new TCEException("信息为空");
		}

		if (ArrayUtil.isEmpty(saveAo.getImageBinary())) {
			throw new TCEException("图片信息为空");
		}

		if (StringUtil.isNullOrEmpty(saveAo.getImagePosition())) {
			throw new TCEException("投放位置为空");
		}

		if (Objects.isNull(AdverPositionEnum.code(saveAo.getImagePosition()))) {
			throw new TCEException("投放位置无效");
		}

		if (StringUtil.isNullOrEmpty(saveAo.getImageLink())) {
			throw new TCEException("跳转链接为空");
		}
	}
}
