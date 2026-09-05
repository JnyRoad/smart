package com.tce.smart.platform.service.securityzone.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.enums.SmtSnapPersonEnum;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.businesstrip.resp.CcdFormtableMainRespDTO;
import com.tce.smart.data.api.dto.consume.resp.WorkTimeRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.EvwLdxRegLeaveAllRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.EvwLregLeaveAllRespDTO;
import com.tce.smart.data.api.feign.consume.RemoteRsEmpService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwLdxRegLeaveAllService;
import com.tce.smart.data.api.feign.ehrview.RemoteEvwLregLeaveAllService;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityAuthDeleteReqDTO;
import com.tce.smart.platform.core.dto.securityzone.SecurityAuthDeleteTaskRef;
import com.tce.smart.platform.core.dto.SearchTravelDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthDelete;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthDeleteLog;
import com.tce.smart.platform.core.mapper.SmtSecurityAuthDeleteMapper;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.securityzone.SmtSecurityAuthDeleteLogService;
import com.tce.smart.platform.service.securityzone.SmtSecurityAuthDeleteService;
import com.tce.smart.platform.service.securityzone.SmtSecurityWhiteService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.OneOrZeroEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fushiping
 * @date 2021-07-29 11:13:24
 */
@Service
public class SmtSecurityAuthDeleteServiceImpl extends ServiceImpl<SmtSecurityAuthDeleteMapper, SmtSecurityAuthDelete> implements SmtSecurityAuthDeleteService {

	private static final Logger log = LoggerFactory.getLogger(SmtSecurityAuthDeleteServiceImpl.class);
	private static final String RESULT_SKIPPED_WHITELIST = "SKIPPED_WHITELIST";
	private static final String RESULT_SKIPPED_NOT_DUE = "SKIPPED_NOT_DUE";
	private static final String RESULT_SKIPPED_NO_DEVICE = "SKIPPED_NO_DEVICE";
	private static final String RESULT_SKIPPED_STAFF_MISSING = "SKIPPED_STAFF_MISSING";
	private static final String RESULT_DRY_RUN = "DRY_RUN";
	private static final String RESULT_PROCESSING = "PROCESSING";
	private static final String RESULT_FAILED = "FAILED";
	private static final String TRIGGER_WHITE_LIST = "命中白名单";
	private static final String TRIGGER_AUTH_CREATE_TIME = "按授权创建时间计算";
	private static final String TRIGGER_LAST_SNAP_TIME = "按最后进出时间计算";
	private static final String TRIGGER_NO_DEVICE = "权限组未关联设备";
	private static final String TRIGGER_STAFF_MISSING = "员工主数据缺失";
	private static final String TRIGGER_AUDIT_FAILURE = "判定过程异常";
	private static final int MAX_REMARK_LENGTH = 1000;

	@Autowired
	private SmtSecurityWhiteService smtSecurityWhiteService;
	@Autowired
	private SmtStaffDeviceAuthService smtStaffDeviceAuthService;
	@Autowired
	private SmtDeviceAuthorityRelationService smtDeviceAuthorityRelationService;
	@Autowired
	private SmtSnapPersonService smtSnapPersonService;
	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private SmtParkBuService smtParkBuService;
	@Autowired
	private SmtOrganizeRelationService smtOrganizeRelationService;
	@Autowired
	private RemoteRsEmpService remoteRsEmpService;
	@Autowired
	private RemoteEvwLdxRegLeaveAllService remoteEvwLdxRegLeaveAllService;
	@Autowired
	private RemoteEvwLregLeaveAllService remoteEvwLregLeaveAllService;
	@Autowired
	private SmtTravelApplicationService smtTravelApplicationService;
	@Autowired
	private SmtDeviceAuthorityService smtDeviceAuthorityService;
	@Autowired
	private SmtSecurityAuthDeleteLogService smtSecurityAuthDeleteLogService;
	@Autowired
	private PlatformTransactionManager transactionManager;

	@Override
	public IPage<SmtSecurityAuthDelete> getList(Page page) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		return this.page(page, Wrappers.<SmtSecurityAuthDelete>lambdaQuery().in(SmtSecurityAuthDelete::getParkId, parkIdList));
	}

	/** 查询园区自动删权配置；新配置默认为正式模式，历史空演练值按0返回。 */
	@Override
	public SmtSecurityAuthDelete getConfig(Integer parkId) {
		SmtSecurityAuthDelete delete = this.getOne(Wrappers.<SmtSecurityAuthDelete>query().lambda().eq(SmtSecurityAuthDelete::getParkId, parkId));
		//当第一次进入配置页时，初始化一条数据
		if (Objects.isNull(delete)) {
			SmtSecurityAuthDelete newDelete = SmtSecurityAuthDelete.builder()
					.createTime(LocalDateTime.now())
					.deleteDay(OneOrZeroEnum.ZERO.getCode())
					.isBusiness(OneOrZeroEnum.ZERO.getCode())
					.isCompensatory(OneOrZeroEnum.ZERO.getCode())
					.isHoliday(OneOrZeroEnum.ZERO.getCode())
					.isLeave(OneOrZeroEnum.ZERO.getCode())
					.isWhiteList(OneOrZeroEnum.ZERO.getCode())
					.dryRun(OneOrZeroEnum.ZERO.getCode())
					.parkId(parkId).build();
			this.save(newDelete);
			return newDelete;
		}
		// 历史配置没有演练字段时按正式模式返回，不在查询接口隐式改写数据库。
		if (delete.getDryRun() == null) {
			delete.setDryRun(OneOrZeroEnum.ZERO.getCode());
		}
		return delete;
	}

	/** 保存园区自动删权配置，校验演练值并兼容旧客户端缺省字段。 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean editConfig(SecurityAuthDeleteReqDTO reqDTO) {
		if (reqDTO == null) {
			throw new SmartException("权限自动删除配置不能为空");
		}
		if (reqDTO.getDryRun() != null
				&& !OneOrZeroEnum.ZERO.getCode().equals(reqDTO.getDryRun())
				&& !OneOrZeroEnum.ONE.getCode().equals(reqDTO.getDryRun())) {
			throw new SmartException("dryRun 参数只能是0或1");
		}
		SmtSecurityAuthDelete existing = null;
		if (reqDTO.getId() != null) {
			existing = this.getById(reqDTO.getId());
		}
		SmtSecurityAuthDelete delete = BeanUtils.transform(SmtSecurityAuthDelete.class, reqDTO);
		if(Objects.isNull(reqDTO.getId()) && Objects.nonNull(reqDTO.getParkId())) {
			Integer count = this.count(Wrappers.<SmtSecurityAuthDelete>query().lambda().eq(SmtSecurityAuthDelete::getParkId, reqDTO.getParkId()));
			if(count > 0) {
				throw new SmartException("该园区配置已存在");
			}
		}
		// 旧客户端未传演练字段时保留原值；新建配置沿用正式模式默认值。
		if (delete.getDryRun() == null) {
			delete.setDryRun(existing == null || existing.getDryRun() == null
					? OneOrZeroEnum.ZERO.getCode() : existing.getDryRun());
		}
		this.saveOrUpdate(delete);
		return smtSecurityWhiteService.editList(reqDTO.getWhiteList(), delete.getId());
	}

	/** 扫描所有启用园区；每条权限单独提交或回滚，失败后独立写入失败审计。 */
	@Override
	public void deleteAuthTask() {
		//查询所有园区权限自动删除策略
		List<SmtSecurityAuthDelete> deleteConfigList = this.list();
		Map<Integer, List<SmtSecurityAuthDelete>> map = deleteConfigList.stream()
				.collect(Collectors.groupingBy(SmtSecurityAuthDelete::getParkId));
		Iterator<Map.Entry<Integer, List<SmtSecurityAuthDelete>>> entries = map.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry<Integer, List<SmtSecurityAuthDelete>> entry = entries.next();
			//园区权限删除配置
			SmtSecurityAuthDelete deleteConfig = entry.getValue().get(0);
			//判断删除逻辑是否开启
			if (Objects.isNull(deleteConfig.getDeleteDay()) || OneOrZeroEnum.ZERO.getCode().equals(deleteConfig.getDeleteDay())) {
				continue;
			}
			//根据园区查询权限，避免后续重复查询员工园区
			List<SmtStaffDeviceAuth> authRelations = smtStaffDeviceAuthService.querySecurityAuth(entry.getKey());
			if (CollUtil.isEmpty(authRelations)) {
				continue;
			}
			for (SmtStaffDeviceAuth authRelation : authRelations) {
				// 每条权限使用真正的 REQUIRES_NEW 事务；失败审计在回滚后另开事务保存。
				processOneAuth(authRelation, deleteConfig);
			}
		}
	}

	/**
	 * 在独立事务中执行一条权限判定，并在事务外接住异常以继续后续权限。
	 */
	private void processOneAuth(SmtStaffDeviceAuth authRelation, SmtSecurityAuthDelete deleteConfig) {
		DeleteAuditContext context = new DeleteAuditContext(authRelation, deleteConfig);
		try {
			executeInNewTransaction(status -> {
				processOneAuthInTransaction(context);
				return null;
			});
		} catch (RuntimeException ex) {
			// 业务事务已经回滚；失败快照必须在另一个独立事务中保存，保存失败继续向外抛出。
			log.error("自动删权单条处理失败，继续处理后续权限，errorType={}", ex.getClass().getName());
			boolean auditFailure = context.auditWriteFailed;
			context.auditWriteFailed = false;
			saveFailureLog(context, ex);
			// 正常业务失败可继续扫描；审计写入失败则不能伪装成成功任务。
			if (auditFailure) {
				throw ex;
			}
		}
	}

	/**
	 * 在 REQUIRES_NEW 事务中完成白名单、阈值、演练及正式删除判定。
	 */
	private void processOneAuthInTransaction(DeleteAuditContext context) {
		SmtSecurityAuthDelete deleteConfig = context.config;
		SmtStaffDeviceAuth authRelation = context.authRelation;
		Long staffId = authRelation.getStaffId();
		context.staff = smtStaffService.getById(staffId);
		context.authority = smtDeviceAuthorityService.getById(authRelation.getAuthId());

		// 白名单命中必须早于进出记录查询，避免为跳过判定读取或伪造最后进出时间。
		if (OneOrZeroEnum.ONE.getCode().equals(deleteConfig.getIsWhiteList())
				&& Boolean.TRUE.equals(smtSecurityWhiteService.isExist(deleteConfig.getId(), staffId))) {
			context.triggerReason = TRIGGER_WHITE_LIST;
			recordLog(context, RESULT_SKIPPED_WHITELIST, TRIGGER_WHITE_LIST,
					"命中白名单，未执行权限删除", Collections.emptyList());
			return;
		}

		// 查询该权限策略关联设备；无设备时也要留下判定快照。
		List<SmtDeviceAuthorityRelation> deviceAuthList = smtDeviceAuthorityRelationService
				.list(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>()
						.eq(SmtDeviceAuthorityRelation::getAuthorityId, authRelation.getAuthId()));
		if (CollUtil.isEmpty(deviceAuthList)) {
			context.triggerReason = TRIGGER_NO_DEVICE;
			recordLog(context, RESULT_SKIPPED_NO_DEVICE, TRIGGER_NO_DEVICE,
					"权限组未关联设备，未执行权限删除", Collections.emptyList());
			return;
		}

		if (context.staff == null) {
			context.triggerReason = TRIGGER_STAFF_MISSING;
			recordLog(context, RESULT_SKIPPED_STAFF_MISSING, TRIGGER_STAFF_MISSING,
					"员工主数据不存在，未执行权限删除", Collections.emptyList());
			return;
		}

		List<String> deviceIds = deviceAuthList.stream().map(SmtDeviceAuthorityRelation::getDeviceId)
				.collect(Collectors.toList());
		List<SmtSnapPerson> snapPeople = smtSnapPersonService.list(Wrappers.<SmtSnapPerson>query().lambda()
				.eq(SmtSnapPerson::getPersonType, SmtSnapPersonEnum.SNAP_PERSON_TYPE1.getType())
				.in(SmtSnapPerson::getDeviceId, deviceIds).eq(SmtSnapPerson::getPersonId, staffId)
				.orderByDesc(SmtSnapPerson::getSnapTime));
		Date anchorTime = latestSnapTime(snapPeople);
		if (anchorTime != null) {
			context.lastSnapTime = anchorTime;
			context.triggerReason = TRIGGER_LAST_SNAP_TIME;
		} else {
			// 无真实进出记录时只用授权创建时间参与计算，审计字段 lastSnapTime 保持为空。
			context.triggerReason = TRIGGER_AUTH_CREATE_TIME;
			anchorTime = authRelation.getCreateTime();
		}
		Boolean isDelete = this.freeDay(deleteConfig, context.staff.getBadge(), anchorTime, new Date());
		if (!Boolean.TRUE.equals(isDelete)) {
			context.triggerReason = thresholdReason(context, false);
			recordLog(context, RESULT_SKIPPED_NOT_DUE, context.triggerReason,
					"未达到权限自动删除天数", Collections.emptyList());
			return;
		}

		if (OneOrZeroEnum.ONE.getCode().equals(normalizeDryRun(deleteConfig.getDryRun()))) {
			context.triggerReason = thresholdReason(context, true);
			recordLog(context, RESULT_DRY_RUN, context.triggerReason,
					"演练模式命中删除条件，未删除权限或生成设备任务", Collections.emptyList());
			return;
		}

		// 先删除本地关联，利用同一事务的唯一性避免并发扫描重复生成设备任务。
		if (!Boolean.TRUE.equals(smtStaffDeviceAuthService.removeById(authRelation.getId()))) {
			throw new IllegalStateException("删除员工权限策略关联失败");
		}
		List<SecurityAuthDeleteTaskRef> taskRefs = smtStaffService.savePersonCardTasksWithResult(DeviceTaskConstants.DEL,
				DateUtil.currentSeconds(), DateUtil.currentSeconds(), context.staff, deviceAuthList);
		if (CollUtil.isEmpty(taskRefs)) {
			throw new IllegalStateException("自动删权未生成设备任务");
		}
		// 正式删除只记录 PROCESSING；设备任务状态由报表查询实时聚合。
		context.triggerReason = thresholdReason(context, true);
		recordLog(context, RESULT_PROCESSING, context.triggerReason,
				"权限关联已删除，设备任务已创建", taskRefs);
	}

	/** 使用真正的 REQUIRES_NEW 边界执行一条自动删权事务。 */
	private <T> T executeInNewTransaction(TransactionCallback<T> callback) {
		TransactionTemplate template = new TransactionTemplate(transactionManager);
		template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		return template.execute(callback);
	}

	/** 保存失败快照；该方法只能在前一个业务事务回滚后被调用。 */
	private void saveFailureLog(DeleteAuditContext context, RuntimeException failure) {
		executeInNewTransaction(status -> {
			String remark = "自动删权处理失败";
			if (failure.getMessage() != null && !failure.getMessage().trim().isEmpty()) {
				remark = remark + ": " + failure.getMessage();
			}
			recordLog(context, RESULT_FAILED, context.triggerReason == null ? TRIGGER_AUDIT_FAILURE : context.triggerReason,
					remark, Collections.emptyList());
			return null;
		});
	}

	/** 保存一条不可变判定快照及其实际任务关联。 */
	private void recordLog(DeleteAuditContext context, String result, String triggerReason, String remark,
			List<SecurityAuthDeleteTaskRef> taskRefs) {
		SmtSecurityAuthDeleteLog logSnapshot = new SmtSecurityAuthDeleteLog();
		logSnapshot.setParkId(context.config.getParkId());
		logSnapshot.setExecTime(LocalDateTime.now());
		logSnapshot.setStaffId(context.authRelation.getStaffId());
		if (context.staff != null) {
			logSnapshot.setStaffBadge(context.staff.getBadge());
			logSnapshot.setStaffName(context.staff.getName());
			logSnapshot.setDepartment(context.staff.getDepName());
		}
		logSnapshot.setAuthId(context.authRelation.getAuthId());
		if (context.authority != null) {
			logSnapshot.setAuthName(context.authority.getAuthorityName());
		}
		if (context.lastSnapTime != null) {
			logSnapshot.setLastSnapTime(LocalDateTime.ofInstant(context.lastSnapTime.toInstant(), ZoneId.systemDefault()));
		}
		logSnapshot.setTriggerReason(triggerReason);
		logSnapshot.setResult(result);
		logSnapshot.setRemark(safeRemark(remark));
		logSnapshot.setCreateTime(LocalDateTime.now());
		try {
			smtSecurityAuthDeleteLogService.record(logSnapshot,
					taskRefs == null ? Collections.emptyList() : taskRefs);
		} catch (RuntimeException ex) {
			context.auditWriteFailed = true;
			throw ex;
		}
		// 主记录已成功写入后只输出结构化键值，避免在应用日志中写入人员姓名、工号等隐私。
		log.info("自动删权审计已保存，logId={}, staffId={}, authId={}, result={}",
				logSnapshot.getId(), logSnapshot.getStaffId(), logSnapshot.getAuthId(), logSnapshot.getResult());
	}

	/** 只保留真实抓拍时间；空时间不参与快照，也不伪造为权限创建时间。 */
	private Date latestSnapTime(List<SmtSnapPerson> snapPeople) {
		if (CollUtil.isEmpty(snapPeople)) {
			return null;
		}
		return snapPeople.stream().map(SmtSnapPerson::getSnapTime).filter(Objects::nonNull)
				.max(Date::compareTo).orElse(null);
	}

	/** 根据真实时间依据和配置阈值生成用户可读的触发原因。 */
	private String thresholdReason(DeleteAuditContext context, boolean due) {
		String basis = context.lastSnapTime == null ? TRIGGER_AUTH_CREATE_TIME : TRIGGER_LAST_SNAP_TIME;
		String suffix = due ? "，超过" : "，未达到";
		return basis + suffix + context.config.getDeleteDay() + "天";
	}

	/** 统一校验演练配置，历史空值按正式模式处理。 */
	private Integer normalizeDryRun(Integer dryRun) {
		if (dryRun == null) {
			return OneOrZeroEnum.ZERO.getCode();
		}
		if (!OneOrZeroEnum.ZERO.getCode().equals(dryRun) && !OneOrZeroEnum.ONE.getCode().equals(dryRun)) {
			throw new SmartException("dryRun 参数只能是0或1");
		}
		return dryRun;
	}

	/** 审计说明长度受表字段约束，截断异常文本而不输出人员隐私到应用日志。 */
	private String safeRemark(String remark) {
		if (remark == null) {
			return null;
		}
		return remark.length() <= MAX_REMARK_LENGTH ? remark : remark.substring(0, MAX_REMARK_LENGTH);
	}

	/** 一条权限处理期间跨事务保留的快照上下文。 */
	private static final class DeleteAuditContext {
		private final SmtStaffDeviceAuth authRelation;
		private final SmtSecurityAuthDelete config;
		private SmtStaff staff;
		private SmtDeviceAuthority authority;
		private Date lastSnapTime;
		private String triggerReason;
		private boolean auditWriteFailed;

		private DeleteAuditContext(SmtStaffDeviceAuth authRelation, SmtSecurityAuthDelete config) {
			this.authRelation = authRelation;
			this.config = config;
		}
	}

	/**
	 * 按园区配置的删除天数及过滤项计算某段未进出时间是否应删除权限。
	 *
	 * @param config 园区自动删除配置；删除天数必须来自 deleteDay，白名单开关不参与阈值计算。
	 * @param badge 员工工号，用于查询节假日、出差、请假与调休记录。
	 * @param startTime 最后一次进出时间；没有进出记录时由调用方传入权限创建时间。
	 * @param endTime 本次任务的判定时间，通常为当前时间。
	 * @return false 表示未超过删除阈值，true 表示超过删除阈值。
	 * <p>该方法不修改本地数据；启用过滤项时会读取远程考勤与业务记录，相关调用异常会向上抛出并中止本次权限删除。</p>
	 */
	private Boolean freeDay(SmtSecurityAuthDelete config, String badge, Date startTime, Date endTime) {
		List<DateTime> dateTimes = DateUtil.rangeToList(startTime, endTime, DateField.DAY_OF_YEAR);
		Integer initDays = dateTimes.size();
		Integer limitDays = config.getDeleteDay();
		if (limitDays >= initDays) {
			return Boolean.FALSE;
		}
		//计算节假日
		if (OneOrZeroEnum.ONE.getCode().equals(config.getIsHoliday())) {
			//获得节假日安排
			Result<WorkTimeRespDTO> result = remoteRsEmpService.getFreeDays(badge, SecurityConstants.FROM_IN);
			if (Objects.nonNull(result.getData())) {
				//移除节假日
				dateTimes.removeAll(result.getData().getTimes());
			}
		}
		//计算出差
		if (OneOrZeroEnum.ONE.getCode().equals(config.getIsBusiness())) {
			Page page = new Page();
			page.setSize(20);
			page.setCurrent(1);
			SearchTravelDTO dto = new SearchTravelDTO();
			dto.setStaffBadge(badge);
			IPage<CcdFormtableMainRespDTO> resp = smtTravelApplicationService.getSmtTravelApplicationPage(page, dto);
			List<CcdFormtableMainRespDTO> record = resp.getRecords();
			if (CollUtil.isNotEmpty(record)) {
				//查询在计算日期内的出差记录
				List<CcdFormtableMainRespDTO> list = record.stream().filter(main -> main.getTripBeginTime().compareTo(startTime) >= 0
						|| main.getTripEndTime().compareTo(startTime) >= 0).collect(Collectors.toList());
				if (CollUtil.isNotEmpty(list)) {
					list.forEach(main -> {
						List<DateTime> tripTime = DateUtil.rangeToList(main.getTripBeginTime(), main.getTripEndTime(), DateField.DAY_OF_YEAR);
						dateTimes.removeAll(tripTime);
					});
				}
			}
		}
		//计算请假
		if (OneOrZeroEnum.ONE.getCode().equals(config.getIsLeave())) {
			Result<List<EvwLregLeaveAllRespDTO>> infoAll = remoteEvwLregLeaveAllService.info(badge, DateUtil.formatDateTime(startTime), DateUtil.formatDateTime(endTime));
			if (CollUtil.isNotEmpty(infoAll.getData())) {
				infoAll.getData().forEach(leave -> {
					List<DateTime> tripTime = DateUtil.rangeToList(leave.getBeginDate(), leave.getEndDate(), DateField.DAY_OF_YEAR);
					dateTimes.removeAll(tripTime);
				});
			}
		}
		//计算调休
		if (OneOrZeroEnum.ONE.getCode().equals(config.getIsCompensatory())) {
			Result<List<EvwLdxRegLeaveAllRespDTO>> reg = remoteEvwLdxRegLeaveAllService.listByDay(badge, DateUtil.formatDate(startTime), SecurityConstants.FROM_IN);
			if (CollUtil.isNotEmpty(reg.getData())) {
				List<DateTime> regTime = new ArrayList<>();
				reg.getData().forEach(leave -> {
					regTime.add(DateTime.of(leave.getBEGINTIME()));
				});
				dateTimes.removeAll(regTime);
			}
		}
		if (limitDays < dateTimes.size()) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	/**
	 * 获得员工的园区ID
	 *
	 * @param staffId
	 * @return
	 */
	private Integer getPark(Long staffId) {
		SmtStaff staff = smtStaffService.getById(staffId);
		SmtParkBu bu = smtParkBuService.getOne(Wrappers.<SmtParkBu>query().lambda().eq(SmtParkBu::getCompId, staff.getCompId()));
		if (Objects.nonNull(bu)) {
			return bu.getParkId();
		}
		SmtOrganizeRelation relation = smtOrganizeRelationService.getByBu(Long.parseLong(staff.getCompId()));
		if (Objects.nonNull(relation)) {
			return relation.getParkId();
		}
		return null;
	}
}
