package com.tce.smart.platform.service.admittance.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.admittance.VisitorManualAuthReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorManualAuthOptionsRespDTO;
import com.tce.smart.platform.core.dto.DeviceTaskVO;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtDeviceAuthority;
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceFellow;
import com.tce.smart.platform.core.mapper.SmtAdmittanceApplyMapper;
import com.tce.smart.platform.core.service.SmtDeviceService;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.service.SmtDeviceAuthorityRelationService;
import com.tce.smart.platform.service.SmtDeviceAuthorityService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceFellowService;
import com.tce.smart.platform.service.admittance.VisitorManualAuthService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.AdmittanceTypeEnum;
import com.tce.smart.tool.enums.DeviceAuthTypeEnum;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTypeEnum;
import com.tce.smart.tool.enums.VisitorStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 访客申请管理端手动下发 ISC 人员权限。
 *
 * 该服务保持手动批次与审批自动提交指针分离，只复用设备任务的统一保存入口，
 * 并在进入保存入口前确认每台设备都具备 ISC 人员任务的完整追溯能力。
 */
@Service
public class VisitorManualAuthServiceImpl extends ServiceImpl<SmtAdmittanceApplyMapper, SmtAdmittanceApply>
		implements VisitorManualAuthService {

	private static final int MAX_AUTHORITY_COUNT = 100;
	private static final String EXISTING_TASK_MESSAGE = "任务已存在";
	private static final String SECRET_AUTH_MESSAGE = "保密考试校验尚未接入，暂不支持下发保密权限";

	@Autowired
	private SmtAdmittanceFellowService smtAdmittanceFellowService;
	@Autowired
	private SmtDeviceAuthorityService smtDeviceAuthorityService;
	@Autowired
	private SmtDeviceAuthorityRelationService smtDeviceAuthorityRelationService;
	@Autowired
	private SmtDeviceService smtDeviceService;
	@Autowired
	private SmtDeviceTaskService smtDeviceTaskService;
	@Autowired
	private TransactionTemplate transactionTemplate;
	@Value("${spring.visitor.put-offset-hour:2}")
	private Integer putOffsetHour;

	/**
	 * 查询申请单当前可用的人员和公共人员权限组。
	 * 车辆列表固定为空，避免把底层无法保存 applyId/batchId 的车辆任务伪装成可选能力。
	 */
	@Override
	public VisitorManualAuthOptionsRespDTO getOptions(Long applyId) {
		List<Integer> parkIds = currentParkIds();
		SmtAdmittanceApply apply = loadAndValidateApply(applyId, parkIds);
		VisitorManualAuthOptionsRespDTO response = new VisitorManualAuthOptionsRespDTO();
		response.setApplyId(String.valueOf(apply.getId()));
		response.setStartTime(formatTime(apply.getStartTime().minusHours(offsetHours())));
		response.setEndTime(formatTime(apply.getEndTime()));

		List<SmtAdmittanceFellow> fellows = smtAdmittanceFellowService.getByApplyId(apply.getId());
		if (fellows != null) {
			response.setFellows(fellows.stream()
					.filter(fellow -> fellow != null && fellow.getId() != null
							&& apply.getId().equals(fellow.getVisitorId()))
					.map(this::toFellowOption)
					.collect(Collectors.toList()));
		}

		List<SmtDeviceAuthority> authorities = findPublicPersonAuthorities(apply.getParkId());
		Map<Integer, List<SmtDeviceAuthorityRelation>> relationsByAuthority = relationMap(authorities);
		for (SmtDeviceAuthority authority : authorities) {
			List<SmtDeviceAuthorityRelation> relations = relationsByAuthority.get(authority.getId());
			// 选项只展示所有关联设备均可由 ISC 人员任务完整承载的权限组；混合组由提交路径明确拒绝。
			if (hasOnlyValidIscPersonDevices(relations, apply.getParkId())) {
				response.getAuthorities().add(toAuthorityOption(authority));
			}
		}
		return response;
	}

	/**
	 * 在申请行锁内重读申请并创建一批人员任务。
	 * 条件更新只用于取得行锁，不写入 iscSubmitBatch，避免手动批次干扰审批自动提交进度。
	 */
	@Override
	public String submit(VisitorManualAuthReqDTO request) {
		if (request == null || request.getApplyId() == null || request.getApplyId() <= 0) {
			throw new SmartException("申请单不存在");
		}
		List<Integer> parkIds = currentParkIds();
		if (transactionTemplate == null) {
			throw new IllegalStateException("手动授权事务未配置");
		}
		String batchId = transactionTemplate.execute(status -> {
			// 先按可下发状态、类型和未过期条件更新自身，利用数据库条件 UPDATE 锁住申请行。
			LambdaUpdateWrapper<SmtAdmittanceApply> lockWrapper = Wrappers.<SmtAdmittanceApply>lambdaUpdate()
					.eq(SmtAdmittanceApply::getId, request.getApplyId())
					.in(SmtAdmittanceApply::getParkId, parkIds)
					.in(SmtAdmittanceApply::getStatus, VisitorStatusEnum.Status_0.getCode(), VisitorStatusEnum.Status_3.getCode())
					.eq(SmtAdmittanceApply::getApplyType, AdmittanceTypeEnum.PERSON.getCode())
					.gt(SmtAdmittanceApply::getEndTime, LocalDateTime.now())
					.setSql("id = id");
			if (!this.update(null, lockWrapper)) {
				throw new SmartException("申请单不存在、已过期或当前状态不可下发");
			}
			// 行锁取得后必须以数据库最新对象重读，不能继续使用请求前查询的旧申请状态或时间。
			SmtAdmittanceApply apply = this.getById(request.getApplyId());
			validateApply(apply, parkIds);
			return submitLocked(request, apply);
		});
		if (StrUtil.isBlank(batchId)) {
			throw new SmartException("权限任务未创建，无法返回批次号");
		}
		return batchId;
	}

	/**
	 * 在已锁定申请内校验对象、权限组和设备，再通过统一任务管线保存 ISC 人员任务。
	 */
	private String submitLocked(VisitorManualAuthReqDTO request, SmtAdmittanceApply apply) {
		if (request.getVehicleId() != null) {
			throw new SmartException("车辆权限暂不支持下发");
		}
		if (request.getFellowId() == null || request.getFellowId() <= 0) {
			throw new SmartException("必须选择本申请中的一名人员");
		}
		SmtAdmittanceFellow fellow = findFellow(apply.getId(), request.getFellowId());
		if (fellow == null) {
			throw new SmartException("人员不属于当前申请");
		}
		if (StrUtil.isBlank(fellow.getFellowPhotoId())) {
			throw new SmartException("人员照片不存在，无法下发");
		}

		List<Integer> authorityIds = normalizeAuthorityIds(request.getAuthIds());
		List<SmtDeviceAuthority> authorities = loadRequestedAuthorities(authorityIds, apply.getParkId());
		Map<Integer, List<SmtDeviceAuthorityRelation>> relationsByAuthority = relationMap(authorities);
		Set<String> deviceIds = validateAndCollectDevices(authorities, relationsByAuthority, apply.getParkId());
		if (deviceIds.isEmpty()) {
			throw new SmartException("权限组暂无可用ISC人员设备，无法下发");
		}

		Long batchId = IdWorker.getId();
		for (String deviceId : deviceIds) {
			DeviceTaskVO task = buildPersonTask(apply, fellow, deviceId, batchId);
			saveRequiredTask(task);
		}
		return String.valueOf(batchId);
	}

	/**
	 * 构造公共人员权限组查询，并在业务层再次核验园区、类型和公共性质，防止 DAO 条件被绕过。
	 */
	private List<SmtDeviceAuthority> findPublicPersonAuthorities(Integer parkId) {
		List<SmtDeviceAuthority> authorities = smtDeviceAuthorityService.list(
				Wrappers.<SmtDeviceAuthority>lambdaQuery()
						.eq(SmtDeviceAuthority::getParkId, parkId)
						.eq(SmtDeviceAuthority::getType, DeviceAuthTypeEnum.PERSON.getCode())
						.eq(SmtDeviceAuthority::getAreaType, 0));
		if (authorities == null) {
			return Collections.emptyList();
		}
		return authorities.stream()
				.filter(authority -> authority != null
						&& authority.getId() != null
						&& Integer.valueOf(DeviceAuthTypeEnum.PERSON.getCode()).equals(authority.getType())
						&& Integer.valueOf(0).equals(authority.getAreaType())
						&& parkId.equals(authority.getParkId()))
				.collect(Collectors.toList());
	}

	/**
	 * 加载请求的权限组并要求每个 ID 都存在，权限组类型只支持现有人员类型 1。
	 */
	private List<SmtDeviceAuthority> loadRequestedAuthorities(List<Integer> authorityIds, Integer parkId) {
		List<SmtDeviceAuthority> authorities = smtDeviceAuthorityService.list(
				Wrappers.<SmtDeviceAuthority>lambdaQuery().in(SmtDeviceAuthority::getId, authorityIds));
		Map<Integer, SmtDeviceAuthority> authorityMap = new LinkedHashMap<>();
		if (authorities != null) {
			for (SmtDeviceAuthority authority : authorities) {
				if (authority != null && authority.getId() != null) {
					authorityMap.put(authority.getId(), authority);
				}
			}
		}
		List<SmtDeviceAuthority> result = new ArrayList<>();
		for (Integer authorityId : authorityIds) {
			SmtDeviceAuthority authority = authorityMap.get(authorityId);
			if (authority == null) {
				throw new SmartException("权限组不存在或无权访问");
			}
			if (!parkId.equals(authority.getParkId())) {
				throw new SmartException("权限组不属于当前申请园区");
			}
			if (Integer.valueOf(1).equals(authority.getAreaType())) {
				throw new SmartException(SECRET_AUTH_MESSAGE);
			}
			if (!Integer.valueOf(0).equals(authority.getAreaType())) {
				throw new SmartException("权限组性质无效");
			}
			if (!Integer.valueOf(DeviceAuthTypeEnum.PERSON.getCode()).equals(authority.getType())) {
				throw new SmartException("仅支持人员权限组");
			}
			result.add(authority);
		}
		return result;
	}

	/**
	 * 读取关联行后同时核对关联园区、设备园区、ISC 标记及人员闸机类型；任一条不支持即整体拒绝。
	 */
	private Set<String> validateAndCollectDevices(List<SmtDeviceAuthority> authorities,
			Map<Integer, List<SmtDeviceAuthorityRelation>> relationsByAuthority, Integer parkId) {
		Set<String> deviceIds = new LinkedHashSet<>();
		for (SmtDeviceAuthority authority : authorities) {
			List<SmtDeviceAuthorityRelation> relations = relationsByAuthority.get(authority.getId());
			if (CollectionUtils.isEmpty(relations)) {
				throw new SmartException("权限组暂无可用ISC人员设备，无法下发");
			}
			for (SmtDeviceAuthorityRelation relation : relations) {
				if (relation == null || !authority.getId().equals(relation.getAuthorityId())
						|| !parkId.equals(relation.getParkId()) || StrUtil.isBlank(relation.getDeviceId())) {
					throw new SmartException("权限组包含不支持的设备，无法下发");
				}
				SmtDevice device = smtDeviceService.getById(relation.getDeviceId());
				if (!isValidIscPersonDevice(device, parkId)) {
					throw new SmartException("权限组包含不支持的设备，无法下发");
				}
				deviceIds.add(relation.getDeviceId());
			}
		}
		return deviceIds;
	}

	/**
	 * 选项页过滤掉无设备、跨园区、非 ISC 或非人员闸机的权限组。
	 */
	private boolean hasOnlyValidIscPersonDevices(List<SmtDeviceAuthorityRelation> relations, Integer parkId) {
		if (CollectionUtils.isEmpty(relations)) {
			return false;
		}
		for (SmtDeviceAuthorityRelation relation : relations) {
			if (relation == null || !parkId.equals(relation.getParkId()) || StrUtil.isBlank(relation.getDeviceId())
					|| !isValidIscPersonDevice(smtDeviceService.getById(relation.getDeviceId()), parkId)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 关联服务原查询不带园区条件，这里在内存中按权限组归集，未知权限组的关联行直接忽略。
	 */
	private Map<Integer, List<SmtDeviceAuthorityRelation>> relationMap(List<SmtDeviceAuthority> authorities) {
		if (CollectionUtils.isEmpty(authorities)) {
			return Collections.emptyMap();
		}
		List<Integer> authorityIds = authorities.stream().map(SmtDeviceAuthority::getId).collect(Collectors.toList());
		List<SmtDeviceAuthorityRelation> relations = smtDeviceAuthorityRelationService.getRelationByAuthId(authorityIds);
		Map<Integer, List<SmtDeviceAuthorityRelation>> result = new LinkedHashMap<>();
		if (relations != null) {
			for (SmtDeviceAuthorityRelation relation : relations) {
				if (relation != null && authorityIds.contains(relation.getAuthorityId())) {
					result.computeIfAbsent(relation.getAuthorityId(), ignored -> new ArrayList<>()).add(relation);
				}
			}
		}
		return result;
	}

	/**
	 * 组装 ISC 卡片下发任务，字段与现有 addCard 语义保持一致，并附加本次手动批次追踪字段。
	 */
	private DeviceTaskVO buildPersonTask(SmtAdmittanceApply apply, SmtAdmittanceFellow fellow,
			String deviceId, Long batchId) {
		DeviceTaskVO task = new DeviceTaskVO();
		task.setDeviceCode(deviceId);
		task.setAction(DeviceTaskActionEnum.DOWN.getCode());
		task.setServiceType(DeviceTaskConstants.CARD_ADMITTANCE);
		task.setCardNo(String.valueOf(fellow.getId()));
		task.setGeneral(fellow.getFellowName());
		task.setCardType(SmtVisitorEnum.CARD_TYPE_7.getType());
		task.setImageId(fellow.getFellowPhotoId());
		task.setDeviceType(DeviceTaskConstants.CARD);
		task.setStartTime(DateUtils.toEpochMilli(apply.getStartTime().plusHours(-offsetHours())) / 1000);
		task.setOverTime(DateUtils.toEpochMilli(apply.getEndTime()) / 1000);
		task.setApplyBadge(fellow.getCertNo());
		task.setApplyId(apply.getId());
		task.setBatchId(batchId);
		return task;
	}

	/**
	 * 统一入口返回空值、重复或非数字结果都视为本批次失败，避免生成没有真实任务的新批次号。
	 */
	private void saveRequiredTask(DeviceTaskVO task) {
		String result = smtDeviceTaskService.saveTask(task);
		if (EXISTING_TASK_MESSAGE.equals(result)) {
			throw new SmartException("权限任务已存在，请勿重复下发");
		}
		if (StrUtil.isBlank(result) || !isPositiveNumber(result.trim())) {
			throw new SmartException("设备任务保存失败" + (StrUtil.isBlank(result) ? "：任务保存结果为空" : "：" + result));
		}
	}

	/**
	 * 只允许已存在申请行、已通过/已到达、人员类型且仍在有效期内的申请进入两条接口。
	 */
	private SmtAdmittanceApply loadAndValidateApply(Long applyId, List<Integer> parkIds) {
		if (applyId == null || applyId <= 0) {
			throw new SmartException("申请单不存在");
		}
		SmtAdmittanceApply apply = this.getById(applyId);
		validateApply(apply, parkIds);
		return apply;
	}

	/**
	 * 申请校验集中于服务端，日期和对象关系均来自数据库，不信任前端重复传入的数据。
	 */
	private void validateApply(SmtAdmittanceApply apply, List<Integer> parkIds) {
		if (apply == null || apply.getId() == null) {
			throw new SmartException("申请单不存在");
		}
		if (apply.getParkId() == null || parkIds == null || !parkIds.contains(apply.getParkId())) {
			throw new SmartException("无权访问当前申请园区");
		}
		if (!Integer.valueOf(AdmittanceTypeEnum.PERSON.getCode()).equals(apply.getApplyType())) {
			throw new SmartException("仅支持访客人员申请");
		}
		if (!VisitorStatusEnum.Status_0.getCode().equals(apply.getStatus())
				&& !VisitorStatusEnum.Status_3.getCode().equals(apply.getStatus())) {
			throw new SmartException("申请单状态不可下发");
		}
		if (apply.getStartTime() == null || apply.getEndTime() == null
				|| apply.getStartTime().isAfter(apply.getEndTime())) {
			throw new SmartException("申请单时间范围无效");
		}
		if (!apply.getEndTime().isAfter(LocalDateTime.now())) {
			throw new SmartException("申请单已过期，无法下发");
		}
	}

	/**
	 * 对象归属必须由申请随行人员表确认，避免跨申请借用同一人员 ID。
	 */
	private SmtAdmittanceFellow findFellow(Long applyId, Long fellowId) {
		List<SmtAdmittanceFellow> fellows = smtAdmittanceFellowService.getByApplyId(applyId);
		if (fellows == null) {
			return null;
		}
		for (SmtAdmittanceFellow fellow : fellows) {
			if (fellow != null && fellowId.equals(fellow.getId()) && applyId.equals(fellow.getVisitorId())) {
				return fellow;
			}
		}
		return null;
	}

	/**
	 * 去重权限组 ID 并限制批量大小；保留首次出现顺序，保证任务顺序可复核。
	 */
	private List<Integer> normalizeAuthorityIds(List<Integer> authIds) {
		if (CollectionUtils.isEmpty(authIds)) {
			throw new SmartException("权限组不能为空");
		}
		if (authIds.size() > MAX_AUTHORITY_COUNT) {
			throw new SmartException("权限组数量必须在1到100组之间");
		}
		Set<Integer> uniqueIds = new LinkedHashSet<>();
		for (Integer authId : authIds) {
			if (authId == null || authId <= 0) {
				throw new SmartException("权限组 ID 无效");
			}
			uniqueIds.add(authId);
		}
		if (uniqueIds.isEmpty() || uniqueIds.size() > MAX_AUTHORITY_COUNT) {
			throw new SmartException("权限组数量必须在1到100组之间");
		}
		return new ArrayList<>(uniqueIds);
	}

	/**
	 * 当前用户可操作的园区只从认证主体读取，空园区范围直接拒绝。
	 */
	private List<Integer> currentParkIds() {
		if (SecurityUtils.getAuthentication() == null) {
			throw new SmartException("未登录，无法操作访客权限");
		}
		SmartUser user = SecurityUtils.getUser();
		if (user == null || CollectionUtils.isEmpty(user.getParkIdList())) {
			throw new SmartException("无可访问园区，无法操作访客权限");
		}
		List<Integer> parkIds = user.getParkIdList().stream().filter(id -> id != null).distinct().collect(Collectors.toList());
		if (parkIds.isEmpty()) {
			throw new SmartException("无可访问园区，无法操作访客权限");
		}
		return parkIds;
	}

	/**
	 * 设备必须在申请园区、标记为 ISC 且为人员闸机类型，非 ISC 路径没有 applyId/batchId 追踪字段。
	 */
	private boolean isValidIscPersonDevice(SmtDevice device, Integer parkId) {
		return device != null && parkId.equals(device.getParkId())
				&& Integer.valueOf(1).equals(device.getIsSync())
				&& Integer.valueOf(1).equals(device.getEnableStatus())
				&& DeviceTypeEnum.DEVICE_TYPE_1.getCode().equals(device.getDeviceType());
	}

	/**
	 * 将申请人员映射为只暴露 ID 和姓名的前端选项，避免泄露证件和照片字段。
	 */
	private VisitorManualAuthOptionsRespDTO.FellowOption toFellowOption(SmtAdmittanceFellow fellow) {
		VisitorManualAuthOptionsRespDTO.FellowOption option = new VisitorManualAuthOptionsRespDTO.FellowOption();
		option.setId(String.valueOf(fellow.getId()));
		option.setName(fellow.getFellowName());
		return option;
	}

	/**
	 * 将已通过校验的公共人员权限组映射为前端可提交的最小字段集合。
	 */
	private VisitorManualAuthOptionsRespDTO.AuthorityOption toAuthorityOption(SmtDeviceAuthority authority) {
		VisitorManualAuthOptionsRespDTO.AuthorityOption option = new VisitorManualAuthOptionsRespDTO.AuthorityOption();
		option.setId(authority.getId());
		option.setAuthorityName(authority.getAuthorityName());
		option.setType(authority.getType());
		option.setAreaType(authority.getAreaType());
		return option;
	}

	/**
	 * 统一按管理端契约输出不含毫秒的本地时间字符串。
	 */
	private String formatTime(LocalDateTime time) {
		return DateUtils.convert(DateUtils.DEFAULT_DATE_TIME_FORMAT, time);
	}

	/**
	 * 取得已注入的访客权限提前小时数，供选项展示和任务时间计算共用。
	 */
	private int offsetHours() {
		return putOffsetHour == null ? 0 : putOffsetHour;
	}

	/**
	 * 任务保存入口必须返回正整数任务 ID，其他结果都不能作为成功批次依据。
	 */
	private boolean isPositiveNumber(String value) {
		if (!value.matches("[1-9][0-9]*")) {
			return false;
		}
		try {
			return Long.parseLong(value) > 0;
		} catch (NumberFormatException ignored) {
			return false;
		}
	}
}
