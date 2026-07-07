package com.tce.smart.platform.service.admittance.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceFellow;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.service.admittance.AdmittancePhotoOpenService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceApplyService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceFellowService;
import com.tce.smart.tool.enums.AdmittanceTypeEnum;
import com.tce.smart.tool.enums.VisitorStatusEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 入厂申请照片开放接口实现。
 * 设计要点（照片拉取 spec §3.1）：
 * - 园区范围只来自应用 token claim（调用方传入），空范围直接短路返回空，禁止全量兜底；
 * - 清单只返回非空 photoId（历史数据存在空值，直接下发会让客户端反复 404 空转）；
 * - 图片存在性不在清单阶段逐张校验（代价高），由 download 阶段对缺图返回 404、客户端跳过。
 */
@Slf4j
@Service
@AllArgsConstructor
public class AdmittancePhotoOpenServiceImpl implements AdmittancePhotoOpenService {

	private final SmtAdmittanceApplyService smtAdmittanceApplyService;

	private final SmtAdmittanceFellowService smtAdmittanceFellowService;

	private final SmtImageService smtImageService;

	@Override
	public List<String> listPendingPhotoIds(List<Integer> allowedParkIds) {
		// 边界显式：应用未绑定园区（或 claim 脏数据被防御为空）时拒绝一切数据，不查库
		if (CollUtil.isEmpty(allowedParkIds)) {
			return Collections.emptyList();
		}
		// 第一步：园区范围内「审批通过、未过期、非车辆」的申请单
		List<SmtAdmittanceApply> applies = smtAdmittanceApplyService.list(Wrappers.<SmtAdmittanceApply>lambdaQuery()
				.select(SmtAdmittanceApply::getId)
				.eq(SmtAdmittanceApply::getStatus, VisitorStatusEnum.Status_0.getCode())
				.gt(SmtAdmittanceApply::getEndTime, LocalDateTime.now())
				.ne(SmtAdmittanceApply::getApplyType, AdmittanceTypeEnum.CAR.getCode())
				.in(SmtAdmittanceApply::getParkId, allowedParkIds));
		if (CollUtil.isEmpty(applies)) {
			return Collections.emptyList();
		}
		List<Long> applyIds = applies.stream().map(SmtAdmittanceApply::getId).collect(Collectors.toList());
		// 第二步：这些申请单下 photoId 非空的随行人员（照片以随行人员为载体，与推送链路口径一致）。
		// 注意：这里只允许 IS NOT NULL，禁止再加 <> '' —— Oracle 中空串即 NULL，
		// <> '' 对所有行恒不成立，会把整个清单过滤成空（2026-07 生产踩坑实录）；
		// 空串/空白的兜底由下方内存过滤完成（方言无关）。
		List<SmtAdmittanceFellow> fellows = smtAdmittanceFellowService.list(Wrappers.<SmtAdmittanceFellow>lambdaQuery()
				.select(SmtAdmittanceFellow::getFellowPhotoId)
				.in(SmtAdmittanceFellow::getVisitorId, applyIds)
				.isNotNull(SmtAdmittanceFellow::getFellowPhotoId));
		// 去重保序；空串/空白过滤在内存完成（MySQL 等方言存在真正的空串，Oracle 空串即 NULL 已被上面拦截）
		Set<String> photoIds = new LinkedHashSet<>();
		for (SmtAdmittanceFellow fellow : fellows) {
			String photoId = fellow.getFellowPhotoId();
			if (photoId != null && !photoId.trim().isEmpty()) {
				photoIds.add(photoId);
			}
		}
		return new ArrayList<>(photoIds);
	}

	@Override
	public byte[] loadPhoto(String photoId) {
		// 不存在返回 null 由控制器映射 404；缺图属数据质量问题记 WARN 便于排查（spec 错误处理约定）
		byte[] bytes = smtImageService.getImageBinaryByCode(photoId);
		if (bytes == null || bytes.length == 0) {
			log.warn("【照片开放接口】photoId={} 无对应图片数据", photoId);
			return null;
		}
		return bytes;
	}
}
