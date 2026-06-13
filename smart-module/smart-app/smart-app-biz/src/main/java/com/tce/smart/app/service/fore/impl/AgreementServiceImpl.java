package com.tce.smart.app.service.fore.impl;

import java.util.List;
import java.util.Objects;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppParkSubject;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppAgreeService;
import com.tce.smart.app.service.AppContentTextService;
import com.tce.smart.app.service.AppParkSubjectService;
import com.tce.smart.app.service.AppSubjectContentTextService;
import com.tce.smart.app.service.fore.AgreementService;
import com.tce.smart.app.vo.fore.AgreementDetailVo;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.tool.constant.DictConstants;

/**
 * 协议服务实现类
 *
 * @author mckaywu
 * @date 2019-06-17 10:03:12
 */
@Service
@NoArgsConstructor
public class AgreementServiceImpl implements AgreementService {

	/**
	 * 字典远程服务接口
	 */
	@Autowired
	private RemoteDictService remoteDictService;

	/**
	 * app协议服务接口
	 */
	@Autowired
	private AppAgreeService appAgreeService;

	/**
	 * app主题文本内容服务接口
	 */
	@Autowired
	private AppSubjectContentTextService subConTextService;

	/**
	 * app文本内容服务接口
	 */
	@Autowired
	private AppContentTextService conTextService;

	/**
	 * app园区主题服务接口
	 */
	@Autowired
	private AppParkSubjectService parkSubService;

	@Override
	public AgreementDetailVo getRootOutAgree(final String parkId) {
		return createAgreementDetail(parkId, DictConstants.AGREE_ROOM_OUT);
	}

	@Override
	public AgreementDetailVo getServiceAgree() {
		return createAgreementDetail(null, DictConstants.AGREE_APP);
	}

	/**
	 * 构造协议详情
	 *
	 * @param parkId   园区ID
	 * @param agreeKey 协议key
	 * @return 协议详情
	 */
	private AgreementDetailVo createAgreementDetail(final String parkId, final String agreeKey) {
		// 查询配置的协议ID
		final Result<List<SysDict>> findByType = remoteDictService.findByType(agreeKey,
				SecurityConstants.FROM_IN);

		// 判断协议是否唯一
		if (!findByType.isSuccess() || findByType.getData().size() != 1) {
			throw new TCEException("协议数据异常");
		}

		final Integer subjectId = Integer.parseInt(findByType.getData().get(0).getValue());

		// 查询园区主题信息
		final List<AppParkSubject> appParkSubjects = parkSubService.getByUnionId(parkId == null ? null:Integer.parseInt(parkId), subjectId);
		AgreementDetailVo agreementDetailVo = new AgreementDetailVo();
		// 园区未配置协议则退出
		if (Objects.nonNull(appParkSubjects)) {
			final AppSubject appSubject = appAgreeService.getById(subjectId);
			if (Objects.isNull(appSubject)) {
				throw new TCEException("获取协议异常");
			}

			// 查询主题内容信息
			final Integer textId = subConTextService.getTextById(appSubject.getId());
			// 查询文本内容详情
			final AppContentText appContentText = conTextService.getById(textId);
			if (Objects.isNull(appContentText)) {
				throw new TCEException("获取协议内容异常");
			}

			agreementDetailVo.setAgreeId(String.valueOf(appSubject.getId()));
			agreementDetailVo.setAgreeName(appSubject.getSubjectName());
			agreementDetailVo.setAgreeContent(appContentText.getTextDesc());
		}

		return agreementDetailVo;
	}

}
