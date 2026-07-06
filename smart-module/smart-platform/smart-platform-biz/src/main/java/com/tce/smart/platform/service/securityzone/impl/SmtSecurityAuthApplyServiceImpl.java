package com.tce.smart.platform.service.securityzone.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.msg.req.SecurityAuthApplyDetailAreaReqDTO;
import com.tce.smart.data.api.dto.msg.req.SecurityAuthApplyDetailReqDTO;
import com.tce.smart.data.api.dto.msg.req.SecurityAuthApplyMainReqDTO;
import com.tce.smart.data.api.dto.msg.req.SendSecurityAuthApplyReqDTO;
import com.tce.smart.data.api.feign.msg.RemoteOaWorkFlowService;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityApplyPersonReqDTO;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityAuthApplyPageQueryReqDTO;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityAuthApplyReqDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityAuthApplyPageRespDTO;
import com.tce.smart.platform.core.ao.SecurityAuthApplyPageQueryAO;
import com.tce.smart.platform.core.entity.SmtMsgTemplate;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtSecurityArea;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.dto.WorkFlowLogDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDataDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthApply;
import com.tce.smart.platform.core.mapper.SmtSecurityAuthApplyMapper;
import com.tce.smart.platform.core.service.SmtMsgTemplateService;
import com.tce.smart.platform.core.service.SmtSecurityAreaService;
import com.tce.smart.platform.service.IOAWorkflowService;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.admittance.SmtOaAreaTypeService;
import com.tce.smart.platform.service.oacallback.OaFinalStatusResolver;
import com.tce.smart.platform.service.oacallback.ProcessRecordItem;
import com.tce.smart.platform.service.oacallback.ProcessRecordWriter;
import com.tce.smart.platform.service.securityzone.SmtOaAreaRelationService;
import com.tce.smart.platform.service.securityzone.SmtSecurityAuthApplyService;
import com.tce.smart.platform.service.securityzone.SmtSecurityTaskDetailsService;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.*;
import com.tce.smart.tool.util.WeChatMsgUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fushiping
 * @date 2021-07-29 11:13:31
 */
@Service
@Slf4j
public class SmtSecurityAuthApplyServiceImpl extends ServiceImpl<SmtSecurityAuthApplyMapper, SmtSecurityAuthApply> implements SmtSecurityAuthApplyService {

	/** OA 对账窗口：只扫描最近 90 天内创建的申请单，避免全表扫描（spec §3.1.3） */
	private static final int RECONCILE_WINDOW_DAYS = 90;
	/** 最小静默期：刚创建不足 5 分钟的申请单大概率回调还在路上，跳过避免与正常回调抢跑 */
	private static final int RECONCILE_MIN_AGE_MINUTES = 5;
	/** 每轮对账单批次大小，控制单次扫描与下发的开销 */
	private static final int RECONCILE_BATCH_SIZE = 200;
	/** 超过该时长仍处于 PENDING 视为异常滞留，需要告警提醒人工关注（spec §5.3） */
	private static final int PENDING_ALARM_HOURS = 24;
	/** 场景1（回调丢失扫描）游标的 Redis key，单游标即可满足需求（简化自入厂对账的三段式游标） */
	private static final String OA_RECONCILE_CURSOR_KEY = "oa:security:auth:cursor";
	/** 微信推送失败重试上限：定时任务 20 分钟一轮即天然重试间隔，3 次后放弃（spec §3） */
	private static final int MAX_MSG_RETRY = 3;
	/** isMsg 终态：连续失败达上限后放弃，不再入扫（0=未发送，1=已发送，2=失败放弃） */
	private static final int MSG_SEND_ABANDONED = 2;

	@Autowired
	private RemoteOaWorkFlowService remoteOaWorkFlowService;
	@Autowired
	private SmtSecurityApplyPersonServiceImpl smtSecurityApplyPersonService;
	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private SmtSecurityTaskDetailsService smtSecurityTaskDetailsService;
	@Autowired
	private SmtOaAreaTypeService smtOaAreaTypeService;
	@Autowired
	private SmtParkService smtParkService;
	@Autowired
	private SmtOaAreaRelationService smtOaAreaRelationService;
	@Autowired
	private SmtMsgTemplateService smtMsgTemplateService;
	@Autowired
	private SmtSecurityAreaService smtSecurityAreaService;
	@Autowired
	private IOAWorkflowService ioaWorkflowService;
	@Autowired
	private OaFinalStatusResolver oaFinalStatusResolver;
	@Autowired
	private ProcessRecordWriter processRecordWriter;
	@Autowired(required = false)
	private StringRedisTemplate stringRedisTemplate;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveApply(SecurityAuthApplyReqDTO reqDTO) {
		String processId;
		//发送OA申请
		processId = this.sendOaProcess(reqDTO);
		if(StrUtil.isEmpty(processId)) {
			throw new SmartException("发起OA审批流程失败");
		}
		if (("-7").equals(processId)) {
			throw new SmartException("请确认申请人是否存在OA上级审批人");
		}
		//存储申请记录
		SmtSecurityAuthApply authApply = BeanUtils.transform(SmtSecurityAuthApply.class, reqDTO);
		authApply.setProcessId(processId);
		authApply.setAreaType(StrUtil.join(SymbolConstants.COMMA, reqDTO.getAreaType()));
		authApply.setCreateTime(LocalDateTime.now());
		authApply.setOaStatus(ApproveListStateEnum.PENDING.getCode());
		authApply.setDeviceStatus(DeviceDownStatusEnum.WAIT.getCode());
		authApply.setTotalNum(reqDTO.getPersonList().size());
		authApply.setIsMsg(OneOrZeroEnum.ZERO.getCode());
		this.save(authApply);
		//获得申请人员
		List<SecurityApplyPersonReqDTO> personList = reqDTO.getPersonList();
		smtSecurityApplyPersonService.savePerson(personList, authApply.getId());
		//初始化权限下发列表
		return smtSecurityTaskDetailsService.initTask(personList, authApply.getId());
	}

	@Override
	public boolean claimOaFinalStatus(Long applyId, Integer finalOaStatus) {
		// CAS 抢占终态：回调 handler 与对账任务共用，只有抢到 PENDING 的一方可触发下发（spec §3.1.1）
		return this.update(Wrappers.<SmtSecurityAuthApply>lambdaUpdate()
				.eq(SmtSecurityAuthApply::getId, applyId)
				.eq(SmtSecurityAuthApply::getOaStatus, ApproveListStateEnum.PENDING.getCode())
				.set(SmtSecurityAuthApply::getOaStatus, finalOaStatus));
	}

	@Override
	public boolean triggerDownDevice(SmtSecurityAuthApply authApply) {
		try {
			smtSecurityTaskDetailsService.downDevice(authApply.getId(), authApply.getApplyBadge());
			// 下发未抛异常才推进主表"已触发下发"状态（修 D4，spec §3.1.3）
			// 注意：主表 device_status 与明细表 SmtSecurityTaskDetails.status 是两套码表，
			// 主表 4=ALRAEDY 表示"已触发下发"，明细表 1=SUCCESS 表示"单台设备下发成功"，切勿混淆。
			this.update(Wrappers.<SmtSecurityAuthApply>lambdaUpdate()
					.eq(SmtSecurityAuthApply::getId, authApply.getId())
					.eq(SmtSecurityAuthApply::getDeviceStatus, DeviceDownStatusEnum.WAIT.getCode())
					.set(SmtSecurityAuthApply::getDeviceStatus, DeviceDownStatusEnum.ALRAEDY.getCode()));
			// Critical 修复：CAS 只更新了 DB，未同步内存中的 authApply.deviceStatus。
			// 调用方（callback handler / 对账任务 / 手动下发 downDevice）后续若基于该实体
			// 再做 MyBatis-Plus 的 updateById，默认按 NOT_NULL 策略回写所有非空字段——若内存
			// 字段仍是调用方传入时的旧值 0（WAIT），就会把上面 CAS 刚写入的 4 覆盖回 0，
			// 导致"下发成功但主表仍显示待下发"。
			// 因此只要 downDevice 未抛异常（走到这里），无论上面的 CAS 是否命中（返回 true/false），
			// 都必须把内存字段同步为 ALRAEDY：
			// - CAS 命中：DB 已从 0→4，内存需跟上，否则被后续 updateById 覆盖回 0；
			// - CAS 未命中（说明 DB 已非 0，大概率已是并发场景下别的调用者写成了 4）：
			//   内存置 4 同样安全且必要，避免 updateById 用过期的 0 覆盖 DB 现有的 4。
			// 失败路径（downDevice 抛异常，进入 catch）不会执行到这里，内存字段保持不变，
			// 交由对账任务按现值重试，不允许在异常路径误推进状态。
			authApply.setDeviceStatus(DeviceDownStatusEnum.ALRAEDY.getCode());
			return true;
		} catch (Exception e) {
			// 带堆栈，保持主表 device_status 现值，由对账任务场景 2 重试（spec §3.1.3）
			log.error("保密区申请权限下发失败：applyId={}", authApply.getId(), e);
			return false;
		}
	}

	@Override
	public Boolean downDevice(Long applyId) {
		SmtSecurityAuthApply authApply = this.getById(applyId);
		// 输入与状态边界校验（spec §3.4，修 D5：原实现记录不存在直接 NPE 且未审批可下发）
		if (Objects.isNull(authApply)) {
			throw new SmartException("申请单不存在");
		}
		if (StrUtil.isBlank(authApply.getProcessId())) {
			throw new SmartException("申请单缺少OA流程编号，无法下发");
		}
		Integer oaStatus = authApply.getOaStatus();
		if (ApproveListStateEnum.REFUSE.getCode().equals(oaStatus)) {
			throw new SmartException("该申请已被OA退回，禁止下发");
		}
		if (ApproveListStateEnum.PENDING.getCode().equals(oaStatus)) {
			// 待审批：实时查 OA 判终态，与回调/对账共用同一套 claim 流程（spec §3.4）
			WorkFlowLogDTO logDTO;
			try {
				logDTO = ioaWorkflowService.query(authApply.getProcessId());
			} catch (Exception e) {
				log.error("手动下发查询OA状态失败：applyId={}", applyId, e);
				throw new SmartException("OA状态查询失败，请稍后重试");
			}
			Integer finalStatus = oaFinalStatusResolver.resolve(logDTO);
			if (Objects.isNull(finalStatus)) {
				throw new SmartException("OA审批未完成，禁止下发");
			}
			if (!claimOaFinalStatus(applyId, finalStatus)) {
				throw new SmartException("状态已被其他任务更新，请刷新后重试");
			}
			if (ApproveListStateEnum.REFUSE.getCode().equals(finalStatus)) {
				throw new SmartException("该申请已被OA退回，禁止下发");
			}
			// 补写过程记录，详情页本地留痕（spec §3.1.3）
			if (logDTO.getResultdata() != null) {
				logDTO.getResultdata().forEach(d ->
						processRecordWriter.write(authApply.getProcessId(), ProcessRecordItem.fromOaLog(d)));
			}
			authApply.setOaStatus(finalStatus);
		}
		// 纵深防御：仅 AGREE 终态允许下发；未知状态值（如 CLOSE/WAITING/null）一律拒绝，
		// 拒绝已知坏值+放行其余的写法在状态枚举扩展时会开口子
		if (!ApproveListStateEnum.AGREE.getCode().equals(authApply.getOaStatus())) {
			throw new SmartException("申请单状态异常，禁止下发");
		}
		// 此时 oa_status=1：触发下发（明细级抢占保证幂等，可安全重试）
		return this.triggerDownDevice(authApply);
	}

	@Override
	public SmtSecurityAuthApply getByProcessId(String processId) {
		return this.getOne(Wrappers.<SmtSecurityAuthApply>query().lambda().eq(SmtSecurityAuthApply::getProcessId, processId));
	}

	/**
	 * 发送OA申请
	 *
	 * @param reqDTO
	 * @return
	 */
	private String sendOaProcess(SecurityAuthApplyReqDTO reqDTO) {
		SendSecurityAuthApplyReqDTO sendSecurityAuthApplyReqDTO = new SendSecurityAuthApplyReqDTO();
		//主申请单
		SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(reqDTO.getApplyBadge());
		SecurityAuthApplyMainReqDTO mainReqDTO = new SecurityAuthApplyMainReqDTO();
		String zero = OneOrZeroEnum.ZERO.getCode().toString();
		mainReqDTO.setLcbh("");
		mainReqDTO.setSqjrqy(reqDTO.getAreaId());
		mainReqDTO.setBadge(reqDTO.getApplyBadge());
		mainReqDTO.setName(staff.getName());
		mainReqDTO.setCompid(staff.getCompId());
		List<SmtSecurityArea> areaList = smtSecurityAreaService.list();
		Map<String, List<Field>> fields = Arrays.stream(mainReqDTO.getClass().getDeclaredFields()).collect(Collectors.groupingBy(Field::getName));
		areaList.forEach(area -> {
			List<Field> fs = fields.get(area.getType());
			if (CollUtil.isEmpty(fs)) {
				return;
			}
			Field field = fs.get(0);
			try {
				field.setAccessible(true);
				field.set(mainReqDTO, zero);
			} catch (Exception e) {
				log.error("保密区区域赋初始值异常: type={}, {}", area.getType(), e.getMessage(), e);
			}
		});
//		mainReqDTO.setAa(zero);
//		mainReqDTO.setBb(zero);
//		mainReqDTO.setCc(zero);
//		mainReqDTO.setFf(zero);
//		mainReqDTO.setDd(zero);
//		mainReqDTO.setEe(zero);
//		mainReqDTO.setGg(zero);
//		mainReqDTO.setHh(zero);
//		mainReqDTO.setJj(zero);
//		mainReqDTO.setTt(zero);
//		mainReqDTO.setKk(zero);
//		mainReqDTO.setLl(zero);
//		mainReqDTO.setQq(zero);
//		mainReqDTO.setWw(zero);
//		mainReqDTO.setRr(zero);
//
//		mainReqDTO.setTiantai(zero);
//		mainReqDTO.setLianban(zero);
//		mainReqDTO.setTwoe(zero);
//		mainReqDTO.setThreee(zero);
//		mainReqDTO.setFoure(zero);
//		mainReqDTO.setFivee(zero);
//		mainReqDTO.setSixe(zero);
//		mainReqDTO.setSeven(zero);
//		mainReqDTO.setEighte(zero);
		mainReqDTO.setOo("");
		mainReqDTO.setSqjinruquyu("");
		if(StringUtils.isNotEmpty(reqDTO.getPermitArea())) {
			mainReqDTO.setOo(reqDTO.getPermitArea());
		}
		if(StringUtils.isNotEmpty(reqDTO.getPermitOldArea())) {
			mainReqDTO.setSqjinruquyu(reqDTO.getPermitOldArea());
		}
		this.setArea(mainReqDTO, reqDTO.getAreaType(), areaList);
		//OA的选项值，为固定值
		mainReqDTO.setSqxm("17");
		sendSecurityAuthApplyReqDTO.setSecurityAuthApplyMainReqDTO(mainReqDTO);
		//申请人员
		List<SecurityApplyPersonReqDTO> applyPersonList = reqDTO.getPersonList();
		if (CollUtil.isEmpty(applyPersonList)) {
			return null;
		}
		List<SecurityAuthApplyDetailReqDTO> personReq = applyPersonList.stream().map(person -> {
			List<String> authName = person.getApplyAuths().stream().map(SecurityApplyPersonReqDTO.ApplyAuth::getAuthName).collect(Collectors.toList());
			SecurityAuthApplyDetailReqDTO p = new SecurityAuthApplyDetailReqDTO();
			p.setSqrbm(person.getStaffDepId());
			p.setSqrgh(person.getBadge());
			p.setSqrzw(person.getStaffJobId());
			p.setSqrxm("17");
			p.setSqsy(StringUtils.join(SymbolConstants.BRANCH, authName) + "申请");
			return p;
		}).collect(Collectors.toList());
		sendSecurityAuthApplyReqDTO.setSecurityAuthApplyDetailReqDTOs(personReq);
		//申请区域
		List<SecurityAuthApplyDetailAreaReqDTO> auth = new ArrayList<>();
		SecurityAuthApplyDetailAreaReqDTO areaReq = new SecurityAuthApplyDetailAreaReqDTO();
		areaReq.setSqjrqy(reqDTO.getAreaId());
		auth.add(areaReq);
		sendSecurityAuthApplyReqDTO.setSecurityAuthApplyDetailAreaReqDTOS(auth);
		Result<String> result = remoteOaWorkFlowService.sendSecurityAuthApply(sendSecurityAuthApplyReqDTO);
		if (!result.isSuccess() || StrUtil.isBlank(result.getData())) {
			log.debug("门禁申请OA提交异常，错误信息：{}", result.getData());
			throw new SmartException("OA流程提交异常，请确认OA是否存在人员信息");
		}
		if (StringUtils.isNotEmpty(result.getData())) {
			return result.getData();
		}
		return null;
	}

	private SecurityAuthApplyMainReqDTO setArea(SecurityAuthApplyMainReqDTO main, List<Integer> check, List<SmtSecurityArea> areaList) {
		if (CollUtil.isEmpty(check)) {
			return main;
		}
		for (Integer id : check) {
			SmtSecurityArea securityArea = areaList.stream().filter(area -> area.getCode().equals(id)).findFirst().orElse(null);
			if (Objects.isNull(securityArea)) {
				continue;
			}
			try {
				Class<? extends SecurityAuthApplyMainReqDTO> aClass = main.getClass();
				Field[] fields = aClass.getDeclaredFields();
				Field field = Arrays.stream(fields).filter(f -> f.getName().equals(securityArea.getType())).findFirst().orElse(null);
				if (Objects.isNull(field)) {
					continue;
				}
				field.setAccessible(true);
				field.set(main, SymbolConstants.ONE_STRING);
			} catch (IllegalAccessException e) {
				log.error("保密区区域设置字段值异常: id={}", id, e);
//				switch (areaEnum) {
//					case ITEM_0:
//						main.setJj(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_1:
//						main.setKk(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_2:
//						main.setLl(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_3:
//						main.setQq(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_4:
//						main.setWw(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_5:
//						main.setRr(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_6:
//						main.setTt(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_7:
//						main.setAa(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_8:
//						main.setBb(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_9:
//						main.setFf(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_10:
//						main.setCc(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_11:
//						main.setDd(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_12:
//						main.setEe(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_13:
//						main.setGg(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_14:
//						main.setHh(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_27:
//						main.setTiantai(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_28:
//						main.setLianban(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_29:
//						main.setTwoe(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_30:
//						main.setThreee(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_31:
//						main.setFoure(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_32:
//						main.setFivee(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_33:
//						main.setSixe(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_34:
//						main.setSeven(SymbolConstants.ONE_STRING);
//						break;
//					case ITEM_35:
//						main.setEighte(SymbolConstants.ONE_STRING);
//						break;
//				}
			}
		}
		return main;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public IPage<SecurityAuthApplyPageRespDTO> getPage(Page page, SecurityAuthApplyPageQueryReqDTO query) {
		SecurityAuthApplyPageQueryAO ao = new SecurityAuthApplyPageQueryAO();
		if (Objects.nonNull(query)) {
			ao = BeanUtils.transform(SecurityAuthApplyPageQueryAO.class, query);
		}
		ao.setParkIds(SecurityUtils.getUser().getParkIdList());
		IPage<SecurityAuthApplyPageRespDTO> pageDTO = this.baseMapper.getPage(page, ao);
		List<SecurityAuthApplyPageRespDTO> record = pageDTO.getRecords();
		record.forEach(r -> {
			SmtPark park = smtParkService.getById(r.getParkId());
			r.setParkName(park.getParkName());
			if (ApproveListStateEnum.AGREE.getCode().equals(r.getOaStatus())
					&& DeviceDownStatusEnum.ALRAEDY.getCode().equals(r.getDeviceStatus())) {
				Long applyId = r.getId();
				//刷新下发状态
				smtSecurityTaskDetailsService.syncTaskStatus(applyId);
				r.setSuccessNum(smtSecurityTaskDetailsService.getCount(applyId, DeviceDownStatusEnum.SUCCESS.getCode()));
				r.setFailNum(smtSecurityTaskDetailsService.getCount(applyId, DeviceDownStatusEnum.FAIL.getCode()));
			}
			r.setOaStatusDesc(ApproveListStateEnum.desc(r.getOaStatus()));
			r.setDeviceStatusDesc(DeviceDownStatusEnum.desc(r.getDeviceStatus()));
		});
		return pageDTO;
	}

	@Override
	public void sendMessage() {
		// 获得已下发且未推送微信的数据（isMsg=2 失败放弃的终态单天然不入扫）
		List<SmtSecurityAuthApply> applyList = this.list(Wrappers.<SmtSecurityAuthApply>query().lambda()
				.eq(SmtSecurityAuthApply::getDeviceStatus, DeviceDownStatusEnum.ALRAEDY.getCode())
				.eq(SmtSecurityAuthApply::getIsMsg, OneOrZeroEnum.ZERO.getCode()));
		if (CollUtil.isEmpty(applyList)) {
			return;
		}
		for (SmtSecurityAuthApply apply : applyList) {
			// 单条 try 包住全流程：任何一单异常不得中断整轮
			// （修复原实现 getSimpleSttaffByBadge 返回 null 时 NPE 卡死其后所有单的 bug）
			try {
				smtSecurityTaskDetailsService.syncTaskStatus(apply.getId());
				Integer initNum = smtSecurityTaskDetailsService.getCount(apply.getId(), DeviceDownStatusEnum.IN_WORK.getCode());
				if (initNum > 0) {
					// 还有下发中的明细，结果未定型：本轮跳过，不推送也不计失败
					continue;
				}
				boolean sent = trySendSecurityMsg(apply);
				if (sent) {
					apply.setIsMsg(OneOrZeroEnum.ONE.getCode());
				} else {
					// 失败计数 +1；历史存量行加列前为 null，按 0 起算
					int retryCount = (apply.getMsgRetryCount() == null ? 0 : apply.getMsgRetryCount()) + 1;
					apply.setMsgRetryCount(retryCount);
					if (retryCount >= MAX_MSG_RETRY) {
						// 达上限置终态放弃，封死无限重发；告警日志留人工排查线索
						apply.setIsMsg(MSG_SEND_ABANDONED);
						log.warn("保密权限微信推送连续失败达上限，放弃重试：processId={}, applyBadge={}, retryCount={}",
								apply.getProcessId(), apply.getApplyBadge(), retryCount);
					}
				}
				this.updateById(apply);
			} catch (Exception e) {
				// 异常单不计失败次数（与「明确发送失败」区分），下一轮重扫自然重试
				log.error("保密权限微信推送处理异常：processId={}, applyBadge={}",
						apply.getProcessId(), apply.getApplyBadge(), e);
			}
		}
	}

	/**
	 * 尝试推送单条保密权限下发结果。
	 * 正文用 smt_msg_template 的 WECHAT_SECURITY_11101 渲染（20 字内，spec §3）：
	 * 「保密权限下发完成 成功{成功数量}/共{总数量}」，thing18 显示申请人姓名。
	 *
	 * @return true=中转服务确认发送成功；false=员工/模板缺失或发送失败（计入失败次数）
	 */
	private boolean trySendSecurityMsg(SmtSecurityAuthApply apply) {
		SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(apply.getApplyBadge());
		if (staff == null) {
			log.warn("保密权限微信推送查不到员工，按一次失败计数：applyBadge={}, processId={}",
					apply.getApplyBadge(), apply.getProcessId());
			return false;
		}
		SmtMsgTemplate template = smtMsgTemplateService.selectByTempCode(SmsTemplateEnum.WECHAT_SECURITY_11101.getCode());
		if (template == null || StrUtil.isEmpty(template.getTempContent())) {
			log.warn("保密权限微信推送模板缺失或内容为空，按一次失败计数：tempCode={}",
					SmsTemplateEnum.WECHAT_SECURITY_11101.getCode());
			return false;
		}
		Integer failNum = smtSecurityTaskDetailsService.getCount(apply.getId(), DeviceDownStatusEnum.FAIL.getCode());
		int totalNum = apply.getTotalNum() == null ? 0 : apply.getTotalNum();
		int successNum = Math.max(0, totalNum - (failNum == null ? 0 : failNum));
		String body = template.getTempContent()
				.replace("{成功数量}", String.valueOf(successNum))
				.replace("{总数量}", String.valueOf(totalNum));
		return Boolean.TRUE.equals(pushWeChatMsg(staff.getName(), body, apply.getApplyBadge()));
	}

	/**
	 * 微信推送 seam：静态调用收敛于此，protected 以便单测子类覆写隔离
	 * （Mockito 2.x 无 mockStatic）。本轮仍走默认模板壳，运维确认可用模板清单后
	 * 换模板只改这里的第一个入参。
	 */
	protected Boolean pushWeChatMsg(String displayName, String body, String badge) {
		return WeChatMsgUtil.sendTemplateMsg(WeChatMsgUtil.DEFAULT_TEMPLATE_NAME, displayName, body, badge, null, null);
	}

	@Override
	public void updateOaStatusTask() {
		// 计数器：贯穿场景1/2，收尾统一打点，便于运营监控对账效果（spec §5.3）
		int scanned = 0;
		int agreed = 0;
		int refused = 0;
		int inProgress = 0;
		int queryFailed = 0;
		int downFailed = 0;

		// ========== 场景1：回调丢失——扫描 PENDING 且已有 processId 的申请单，主动向 OA 查询终态 ==========
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime windowStart = now.minusDays(RECONCILE_WINDOW_DAYS);
		LocalDateTime windowEnd = now.minusMinutes(RECONCILE_MIN_AGE_MINUTES);
		long cursor = readCursor();
		List<SmtSecurityAuthApply> batch = pendingOaStatusBatch(windowStart, windowEnd, cursor);
		if (CollUtil.isEmpty(batch) && cursor > 0) {
			// 本轮扫到头且游标非零：归零后下一轮从头重扫，避免游标之前漏掉的单永远扫不到
			updateCursor(0L);
		} else {
			advanceCursor(batch);
		}
		for (SmtSecurityAuthApply apply : batch) {
			scanned++;
			if (apply.getCreateTime() != null && apply.getCreateTime().isBefore(now.minusHours(PENDING_ALARM_HOURS))) {
				log.warn("保密门禁申请超24小时未收到OA终态：processId={}", apply.getProcessId());
			}
			WorkFlowLogDTO dto;
			try {
				dto = ioaWorkflowService.query(apply.getProcessId());
			} catch (Exception e) {
				// 查询异常不中断本轮其余单，下一轮游标归零后天然重扫该单
				log.warn("保密门禁OA对账查询失败：processId={}", apply.getProcessId(), e);
				queryFailed++;
				continue;
			}
			Integer finalStatus = oaFinalStatusResolver.resolve(dto);
			if (finalStatus == null) {
				// 审批中/查询无有效结果，本轮不动，等下一轮再查
				inProgress++;
				continue;
			}
			if (!this.claimOaFinalStatus(apply.getId(), finalStatus)) {
				// CAS 未抢到：说明回调已先一步处理，正常并发场景，跳过
				continue;
			}
			if (ApproveListStateEnum.AGREE.getCode().equals(finalStatus)) {
				agreed++;
				writeProcessRecords(apply.getProcessId(), dto);
				// 内存同步终态，供下面 triggerDownDevice 使用最新状态
				apply.setOaStatus(finalStatus);
				if (!this.triggerDownDevice(apply)) {
					downFailed++;
				}
			} else {
				refused++;
			}
		}

		// ========== 场景2：审批已过但下发未执行（D4 中间态 + 场景1下发失败的重试）==========
		List<SmtSecurityAuthApply> pendingDownList = pendingDownDeviceBatch(windowStart);
		for (SmtSecurityAuthApply apply : pendingDownList) {
			if (!this.triggerDownDevice(apply)) {
				downFailed++;
			}
		}

		log.info("保密门禁OA对账完成：扫描={}, 通过={}, 退回={}, 审批中={}, 查询失败={}, 触发失败={}",
				scanned, agreed, refused, inProgress, queryFailed, downFailed);
	}

	/** 场景1候选：oa_status=PENDING 且已有 processId，创建时间落在对账窗口内，游标翻页取最旧的一批 */
	private List<SmtSecurityAuthApply> pendingOaStatusBatch(LocalDateTime windowStart, LocalDateTime windowEnd, long cursor) {
		Page<SmtSecurityAuthApply> page = new Page<>(1, RECONCILE_BATCH_SIZE);
		page.setSearchCount(false);
		return this.page(page, Wrappers.<SmtSecurityAuthApply>query().lambda()
				.eq(SmtSecurityAuthApply::getOaStatus, ApproveListStateEnum.PENDING.getCode())
				.isNotNull(SmtSecurityAuthApply::getProcessId)
				.between(SmtSecurityAuthApply::getCreateTime, windowStart, windowEnd)
				.gt(SmtSecurityAuthApply::getId, cursor)
				.orderByAsc(SmtSecurityAuthApply::getId)).getRecords();
	}

	/** 场景2候选：oa_status=AGREE 但 device_status=WAIT（审批已过但未下发），量小无需游标 */
	private List<SmtSecurityAuthApply> pendingDownDeviceBatch(LocalDateTime windowStart) {
		Page<SmtSecurityAuthApply> page = new Page<>(1, RECONCILE_BATCH_SIZE);
		page.setSearchCount(false);
		return this.page(page, Wrappers.<SmtSecurityAuthApply>query().lambda()
				.eq(SmtSecurityAuthApply::getOaStatus, ApproveListStateEnum.AGREE.getCode())
				.eq(SmtSecurityAuthApply::getDeviceStatus, DeviceDownStatusEnum.WAIT.getCode())
				.ge(SmtSecurityAuthApply::getCreateTime, windowStart)
				.orderByAsc(SmtSecurityAuthApply::getId)).getRecords();
	}

	/** 补写过程记录：query 到的流转记录逐条落库，写失败仅记 warn，不阻断后续下发（spec §3.1.3） */
	private void writeProcessRecords(String processId, WorkFlowLogDTO dto) {
		List<WorkFlowLogDataDTO> records = dto.getResultdata();
		if (CollUtil.isEmpty(records)) {
			return;
		}
		for (WorkFlowLogDataDTO record : records) {
			try {
				processRecordWriter.write(processId, ProcessRecordItem.fromOaLog(record));
			} catch (Exception e) {
				log.warn("保密门禁OA对账补写过程记录失败：processId={}", processId, e);
			}
		}
	}

	/** 读取场景1游标；Redis 不可用或无值时视为 0（从头扫） */
	private long readCursor() {
		if (stringRedisTemplate == null) {
			return 0L;
		}
		try {
			String value = stringRedisTemplate.opsForValue().get(OA_RECONCILE_CURSOR_KEY);
			return StrUtil.isBlank(value) ? 0L : Long.parseLong(value);
		} catch (Exception e) {
			log.warn("读取保密门禁OA对账游标失败", e);
			return 0L;
		}
	}

	/** 按本批最大 id 推进游标 */
	private void advanceCursor(List<SmtSecurityAuthApply> batch) {
		batch.stream().map(SmtSecurityAuthApply::getId).filter(Objects::nonNull)
				.max(Long::compareTo).ifPresent(this::updateCursor);
	}

	/** 写入游标；Redis 不可用时静默跳过（下一轮 readCursor 会退回 0，等价于持续从头扫） */
	private void updateCursor(long cursor) {
		if (stringRedisTemplate == null) {
			return;
		}
		try {
			stringRedisTemplate.opsForValue().set(OA_RECONCILE_CURSOR_KEY, String.valueOf(cursor));
		} catch (Exception e) {
			log.warn("写入保密门禁OA对账游标失败：cursor={}", cursor, e);
		}
	}
}
