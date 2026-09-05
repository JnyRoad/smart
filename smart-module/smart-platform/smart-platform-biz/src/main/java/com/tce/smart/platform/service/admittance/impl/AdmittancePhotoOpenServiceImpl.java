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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 入厂申请照片开放接口实现。
 * 设计要点（照片拉取 spec §3.1）：
 * - 园区范围只来自应用 token claim（调用方传入），空范围直接短路返回空，禁止全量兜底；
 * - 清单只返回非空 photoId（历史数据存在空值，直接下发会让客户端反复 404 空转）；
 * - 下载按目标照片关联的申请重新校验园区和有效状态，通过后才读取图片；无权或缺图统一返回 404。
 */
@Slf4j
@Service
@AllArgsConstructor
public class AdmittancePhotoOpenServiceImpl implements AdmittancePhotoOpenService {

	/**
	 * Oracle IN 列表单批上限：表达式超过 1000 个直接 ORA-01795，
	 * 清单查询和下载授权均须按此上限分批查询申请关联。
	 */
	private static final int ORACLE_IN_MAX_EXPRESSIONS = 1000;

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
		// 申请单 ID 按 1000 一批拆分 IN 查询（Oracle IN 列表上限，超限即 ORA-01795），各批结果合并。
		// 注意：CollUtil.split 对空集合会返回含一个空子列表的 [[]]（空批进 in() 会生成非法 SQL），
		// 此处依赖上方 applies 判空早退保证 applyIds 非空——复制本分批模式到别处时必须带上判空。
		List<SmtAdmittanceFellow> fellows = new ArrayList<>();
		for (List<Long> applyIdBatch : CollUtil.split(applyIds, ORACLE_IN_MAX_EXPRESSIONS)) {
			fellows.addAll(smtAdmittanceFellowService.list(Wrappers.<SmtAdmittanceFellow>lambdaQuery()
					.select(SmtAdmittanceFellow::getFellowPhotoId)
					.in(SmtAdmittanceFellow::getVisitorId, applyIdBatch)
					.isNotNull(SmtAdmittanceFellow::getFellowPhotoId)));
		}
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

	/** 按 token 园区及有效申请关联授权读图；无权或缺图返回 null，仅执行查询，不写数据。 */
	@Override
	public byte[] loadPhoto(String photoId, List<Integer> allowedParkIds) {
		// 先检查业务授权，避免仅凭 UUID 读到无权访问的图片。
		if (CollUtil.isEmpty(allowedParkIds) || !hasAuthorizedApplication(photoId, allowedParkIds)) {
			return null;
		}
		// 获授权后才读取图片字节；缺图与无权由控制器统一映射为 404。
		byte[] bytes = smtImageService.getImageBinaryByCode(photoId);
		if (bytes == null || bytes.length == 0) {
			log.warn("【照片开放接口】photoId={} 无对应图片数据", photoId);
			return null;
		}
		return bytes;
	}

	/** 判断目标照片是否关联获授权园区内的有效入厂申请；无关联或失效返回 false，仅查询关联信息。 */
	private boolean hasAuthorizedApplication(String photoId, List<Integer> allowedParkIds) {
		// 先按目标照片定位申请 ID，避免为单张下载扫描整个待拉取清单。
		List<SmtAdmittanceFellow> fellows = smtAdmittanceFellowService.list(Wrappers.<SmtAdmittanceFellow>lambdaQuery()
				.select(SmtAdmittanceFellow::getVisitorId)
				.eq(SmtAdmittanceFellow::getFellowPhotoId, photoId));
		List<Long> applyIds = fellows.stream().map(SmtAdmittanceFellow::getVisitorId)
				.filter(Objects::nonNull).distinct().collect(Collectors.toList());
		if (applyIds.isEmpty()) {
			return false;
		}
		// 同一照片可能被多个申请引用；逐批检查任一有效关联，条件与 pending 保持一致。
		LocalDateTime now = LocalDateTime.now();
		for (List<Long> applyIdBatch : CollUtil.split(applyIds, ORACLE_IN_MAX_EXPRESSIONS)) {
			List<SmtAdmittanceApply> applies = smtAdmittanceApplyService.list(Wrappers.<SmtAdmittanceApply>lambdaQuery()
					.select(SmtAdmittanceApply::getId)
					.in(SmtAdmittanceApply::getId, applyIdBatch)
					.eq(SmtAdmittanceApply::getStatus, VisitorStatusEnum.Status_0.getCode())
					.gt(SmtAdmittanceApply::getEndTime, now)
					.ne(SmtAdmittanceApply::getApplyType, AdmittanceTypeEnum.CAR.getCode())
					.in(SmtAdmittanceApply::getParkId, allowedParkIds));
			if (!applies.isEmpty()) {
				return true;
			}
		}
		return false;
	}
}
