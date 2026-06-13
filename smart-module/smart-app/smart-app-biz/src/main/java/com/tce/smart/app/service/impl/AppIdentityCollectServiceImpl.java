package com.tce.smart.app.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.api.dto.AddIdCollectDto;
import com.tce.smart.app.dto.fore.OcrIdCardDto;
import com.tce.smart.app.emun.EmpInfoCompState;
import com.tce.smart.app.entity.AppIdentityCollect;
import com.tce.smart.app.mapper.AppIdentityCollectMapper;
import com.tce.smart.app.service.AppIdentityCollectService;
import com.tce.smart.common.core.constant.enums.SexType;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.tool.enums.EmpImgSyncEnum;

import io.netty.util.internal.StringUtil;

/**
 * App版本控制
 *
 * @author mingkai.wu
 * @date 2019-04-25 11:31:36
 */
@Service
public class AppIdentityCollectServiceImpl extends ServiceImpl<AppIdentityCollectMapper, AppIdentityCollect>
		implements AppIdentityCollectService {

	@Override
	public AppIdentityCollect getInfoByBadge(String badge) {
		return this.getOne(Wrappers.<AppIdentityCollect>query().lambda().eq(AppIdentityCollect::getStaffId, badge));
	}

	@Override
	public int insertOrUpdate(OcrIdCardDto ocrIdCardDto) {
		if (Objects.isNull(ocrIdCardDto) || Objects.isNull(ocrIdCardDto.getStaffId())) {
			return -1;
		}

		AppIdentityCollect appIdentityCollect = this.getInfoByBadge(ocrIdCardDto.getStaffId());
		// 设置ID,用于配判是否是更新操作
		ocrIdCardDto.setId(Objects.nonNull(appIdentityCollect) ? appIdentityCollect.getId() : null);
		AppIdentityCollect preSavePo = this.buildSavePo(ocrIdCardDto);// 实体转换
		if (Objects.nonNull(appIdentityCollect)) {
			// 更新
			this.updateById(preSavePo);
			return appIdentityCollect.getId();
		} else {
			// 新增
			this.save(preSavePo);
			return preSavePo.getId();
		}
	}

	@Override
	public List<AppIdentityCollect> getByStaffId(String staffId) {
		QueryWrapper<AppIdentityCollect> queryWrapper = new QueryWrapper<AppIdentityCollect>();
		// 根据员工号查询
		queryWrapper.lambda().eq(AppIdentityCollect::getStaffId, staffId);
		return baseMapper.selectList(queryWrapper);
	}

	@Override
	public List<AppIdentityCollect> getByIdentity(String identity) {
		QueryWrapper<AppIdentityCollect> queryWrapper = new QueryWrapper<AppIdentityCollect>();
		// 根据身份证号查询
		queryWrapper.lambda().eq(AppIdentityCollect::getIdentityCard, identity);
		return baseMapper.selectList(queryWrapper);
	}

	@Override
	public boolean updatePhtoSync(Integer perfectId, String syncState) {
		AppIdentityCollect appIdentityCollect = new AppIdentityCollect();
		appIdentityCollect.setId(perfectId);
		appIdentityCollect.setPhotoSyncFlag(syncState);
		return this.updateById(appIdentityCollect);
	}

	@Override
	public boolean saveFaceCollect(AddIdCollectDto addIdCollectDto) {
		if (Objects.isNull(addIdCollectDto)) {
			return false;
		}

		AppIdentityCollect appIdentityCollect = new AppIdentityCollect();
		appIdentityCollect.setStaffId(addIdCollectDto.getBadge());
		appIdentityCollect.setFaceImage(addIdCollectDto.getFacePhoto());

		// 插入时间
		appIdentityCollect.setCreateTime(LocalDateTime.now());
		// 完善状态
		appIdentityCollect.setCollectFlag(EmpInfoCompState.USED.getCode());

		String photoSyncFlag = addIdCollectDto.getPhotoSyncFla();
		// 同步状态-初始化（未同步）
		if (StringUtil.isNullOrEmpty(photoSyncFlag)) {
			photoSyncFlag = EmpImgSyncEnum.INIT.getCode();
		}
		appIdentityCollect.setPhotoSyncFlag(photoSyncFlag);

		// 保存到数据库
		this.save(appIdentityCollect);

		return true;
	}

	@Override
	public IPage<AppIdentityCollect> getLatestPhoto(Page<AppIdentityCollect> page) {
		QueryWrapper<AppIdentityCollect> queryWrapper = new QueryWrapper<AppIdentityCollect>();
		queryWrapper
			.lambda()
				.ne(AppIdentityCollect::getPhotoSyncFlag, EmpImgSyncEnum.SUCCESS.getCode())
				.orderByDesc(AppIdentityCollect::getCreateTime)
				.orderByDesc(AppIdentityCollect::getUpdateTime);

		return this.page(page, queryWrapper);
	}

	/**
	 * Dto转换数据库表实体
	 *
	 * @param ocrIdCardDto 信息完善Dto
	 * @return 数据库表实体
	 */
	private AppIdentityCollect buildSavePo(OcrIdCardDto ocrIdCardDto) {

		AppIdentityCollect appIdentityCollect = new AppIdentityCollect();
		appIdentityCollect.setStaffId(ocrIdCardDto.getStaffId());

		if (!StringUtil.isNullOrEmpty(ocrIdCardDto.getIdentityCard())) {
			appIdentityCollect.setIdentityCard(ocrIdCardDto.getIdentityCard());
		}

		if (!StringUtil.isNullOrEmpty(ocrIdCardDto.getName())) {
			appIdentityCollect.setName(ocrIdCardDto.getName());
		}

		if (!StringUtil.isNullOrEmpty(ocrIdCardDto.getGender())) {
			appIdentityCollect.setGender(SexType.code(ocrIdCardDto.getGender()).toString());
		}

		// 民族
		if (!StringUtil.isNullOrEmpty(ocrIdCardDto.getEthnicity())) {
			appIdentityCollect.setEthnicity(ocrIdCardDto.getEthnicity());
		}

		// 生日
		if (!StringUtil.isNullOrEmpty(ocrIdCardDto.getBirthday())) {
			appIdentityCollect.setBirthday(ocrIdCardDto.getBirthday());
		}

		// 地址
		if (!StringUtil.isNullOrEmpty(ocrIdCardDto.getAddress())) {
			appIdentityCollect.setAddress(ocrIdCardDto.getAddress());
		}

		// 签发机构
		if (!StringUtil.isNullOrEmpty(ocrIdCardDto.getSignOrg())) {
			appIdentityCollect.setSignOrg(ocrIdCardDto.getSignOrg());
		}

		// 签发日期
		String signDate = ocrIdCardDto.getSignDate();
		if (StringUtils.isNotEmpty(signDate)) {
			appIdentityCollect
					.setSignDate(LocalDate.parse(signDate, DateTimeFormatter.ofPattern(DateUtils.DATE_FORMAT)));
		}

		// 有效期限
		if (!StringUtil.isNullOrEmpty(ocrIdCardDto.getValidityDate())) {
			appIdentityCollect.setValidityDate(ocrIdCardDto.getValidityDate());
		}

		// 有效期至
		String validityEndDate = ocrIdCardDto.getValidityEndDate();
		if (StringUtils.isNotEmpty(validityEndDate)) {
			appIdentityCollect.setValidityEndDate(
					LocalDate.parse(validityEndDate, DateTimeFormatter.ofPattern(DateUtils.DATE_FORMAT)));
		}

		// 身份证正面照片
		if (!StringUtil.isNullOrEmpty(ocrIdCardDto.getIdCardFrontPhoto())) {
			appIdentityCollect.setFrontImage(ocrIdCardDto.getIdCardFrontPhoto());
		}

		// 身份证背面照片
		if (!StringUtil.isNullOrEmpty(ocrIdCardDto.getIdCardBackPhoto())) {
			appIdentityCollect.setBackImage(ocrIdCardDto.getIdCardBackPhoto());
		}

		// 人脸照片
		if (!StringUtil.isNullOrEmpty(ocrIdCardDto.getFacePhoto())) {
			appIdentityCollect.setFaceImage(ocrIdCardDto.getFacePhoto());
		}

		LocalDateTime nowTime = LocalDateTime.now();
		Integer id = ocrIdCardDto.getId();
		if (Objects.nonNull(id)) {
			appIdentityCollect.setId(id);
			// 更新时间
			appIdentityCollect.setUpdateTime(nowTime);
		} else {
			// 插入时间
			appIdentityCollect.setCreateTime(nowTime);
		}

		return appIdentityCollect;
	}
}
